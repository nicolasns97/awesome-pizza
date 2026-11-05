package com.adesso.awesomepizza.order;

import com.adesso.awesomepizza.menu.PizzaEntity;
import com.adesso.awesomepizza.menu.PizzaNotAvailableException;
import com.adesso.awesomepizza.menu.PizzaRepository;
import com.adesso.awesomepizza.order.api.CreateOrderRequestDto;
import com.adesso.awesomepizza.order.api.OrderItemRequestDto;
import com.adesso.awesomepizza.order.queue.OrderQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderQueueFactory queueFactory;
    private final PizzaRepository pizzaRepository;
    private final TrackingCodeGenerator trackingCodeGenerator;

    public OrderService(OrderRepository orderRepository, OrderQueueFactory queueFactory, PizzaRepository pizzaRepository,
                        TrackingCodeGenerator trackingCodeGenerator) {
        this.orderRepository = orderRepository;
        this.pizzaRepository = pizzaRepository;
        this.trackingCodeGenerator = trackingCodeGenerator;
        this.queueFactory = queueFactory;
    }

    public OrderEntity createOrder(CreateOrderRequestDto request) {
        OrderEntity newOrder = new OrderEntity(trackingCodeGenerator.generate());
        for (OrderItemRequestDto itemRequest : request.items()) {
            PizzaEntity pizza = pizzaRepository.findById(itemRequest.pizzaId())
                    .filter(PizzaEntity::isAvailable)
                    .orElseThrow(() -> new PizzaNotAvailableException(itemRequest.pizzaId()));
            newOrder.addItem(new OrderItemEntity(pizza, itemRequest.quantity(), itemRequest.notes()));
        }
        newOrder.assignPriorityScore(queueFactory.getActiveStrategy().calculatePriorityScore(newOrder));
        OrderEntity savedOrder = orderRepository.save(newOrder);
        log.info("Order created trackingCode={} items={} total={}",
                savedOrder.getTrackingCode(), savedOrder.getItems().size(), savedOrder.getTotal());
        return savedOrder;
    }

    public Optional<OrderEntity> takeNextOrder() {
        return queueFactory.getActiveStrategy().fetchNextOrder()
                .map(order -> {
                    order.startPreparation();
                    log.info("Order taken trackingCode={} waitedSeconds={}",
                            order.getTrackingCode(), secondsBetween(order.getCreatedAt(), order.getStartedAt()));
                    return withItems(order);
                });
    }

    public OrderEntity completeOrder(String trackingCode) {
        OrderEntity order = findWithLock(trackingCode);
        order.completeOrder();
        log.info("Order completed trackingCode={} preparationSeconds={}",
                order.getTrackingCode(), secondsBetween(order.getStartedAt(), order.getCompletedAt()));
        return withItems(order);
    }

    public OrderEntity cancelOrder(String trackingCode) {
        OrderEntity order = findWithLock(trackingCode);
        order.cancelOrder();
        log.info("Order canceled trackingCode={}", order.getTrackingCode());
        return withItems(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderEntity> listOrders(Set<OrderStatus> statuses, Pageable pageable) {
        Page<Long> ids = orderRepository.findIdsInQueueOrder(statuses, pageable);
        List<OrderEntity> orders = ids.isEmpty()
                ? List.of()
                : orderRepository.findWithItemsByIdInQueueOrder(ids.getContent());
        return new PageImpl<>(orders, pageable, ids.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderEntity getOrder(String trackingCode) {
        return orderRepository.findWithItemsByTrackingCode(trackingCode)
                .orElseThrow(() -> new OrderNotFoundException(trackingCode));
    }

    private OrderEntity withItems(OrderEntity lockedOrder) {
        return orderRepository.findWithItemsByTrackingCode(lockedOrder.getTrackingCode()).orElseThrow();
    }

    private OrderEntity findWithLock(String trackingCode) {
        try {
            return orderRepository.findWithLockByTrackingCode(trackingCode)
                    .orElseThrow(() -> new OrderNotFoundException(trackingCode));
        } catch (PessimisticLockingFailureException ex) {
            log.warn("Order lock timed out trackingCode={}", trackingCode);
            throw new OrderLockedException(trackingCode);
        }
    }

    private static long secondsBetween(Instant from, Instant to) {
        return Duration.between(from, to).toSeconds();
    }
}
