package com.adesso.awesomepizza.order;

import com.adesso.awesomepizza.menu.PizzaEntity;
import com.adesso.awesomepizza.menu.PizzaNotAvailableException;
import com.adesso.awesomepizza.menu.PizzaRepository;
import com.adesso.awesomepizza.order.api.CreateOrderRequestDto;
import com.adesso.awesomepizza.order.api.OrderItemRequestDto;
import com.adesso.awesomepizza.order.queue.OrderQueueFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class OrderService {

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
        return orderRepository.save(newOrder);
    }

    public Optional<OrderEntity> takeNextOrder() {
        return queueFactory.getActiveStrategy().fetchNextOrder()
                .map(order -> {
                    order.startPreparation();
                    return withItems(order);
                });
    }

    public OrderEntity completeOrder(String trackingCode) {
        OrderEntity order = findWithLock(trackingCode);
        order.completeOrder();
        return withItems(order);
    }

    public OrderEntity cancelOrder(String trackingCode) {
        OrderEntity order = findWithLock(trackingCode);
        order.cancelOrder();
        return withItems(order);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> listOrders(Set<OrderStatus> statuses) {
        return orderRepository.findAllInQueueOrder(statuses);
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
            throw new OrderLockedException(trackingCode);
        }
    }
}
