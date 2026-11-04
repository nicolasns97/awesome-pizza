package com.adesso.awesomepizza.order.queue;

import com.adesso.awesomepizza.order.OrderEntity;

import java.util.Optional;

public interface OrderQueueStrategy {
    String getStrategyName();

    Optional<OrderEntity> fetchNextOrder();

    default int calculatePriorityScore(OrderEntity order) {
        return 0;
    }
}
