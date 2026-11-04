package com.adesso.awesomepizza.order.queue;

import com.adesso.awesomepizza.order.OrderEntity;
import com.adesso.awesomepizza.order.OrderRepository;
import com.adesso.awesomepizza.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FifoQueueStrategy implements OrderQueueStrategy {
    private final OrderRepository orderRepository;

    public FifoQueueStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public String getStrategyName() {
        return "FIFO";
    }

    @Override
    public Optional<OrderEntity> fetchNextOrder() {
        return orderRepository.findNextFifoOrder(OrderStatus.CREATED);
    }
}
