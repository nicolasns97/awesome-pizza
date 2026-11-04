package com.adesso.awesomepizza.order.queue;

import com.adesso.awesomepizza.order.OrderEntity;
import com.adesso.awesomepizza.order.OrderRepository;
import com.adesso.awesomepizza.order.OrderStatus;

import java.util.Optional;

public abstract class PriorityScoreQueueStrategy implements OrderQueueStrategy {
    private final OrderRepository orderRepository;

    protected PriorityScoreQueueStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public final Optional<OrderEntity> fetchNextOrder() {
        return orderRepository.findNextPriorityOrder(OrderStatus.CREATED);
    }

    @Override
    public abstract int calculatePriorityScore(OrderEntity order);
}
