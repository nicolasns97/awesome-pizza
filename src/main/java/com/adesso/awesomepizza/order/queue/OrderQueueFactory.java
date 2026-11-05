package com.adesso.awesomepizza.order.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderQueueFactory {

    private static final Logger log = LoggerFactory.getLogger(OrderQueueFactory.class);

    private final OrderQueueStrategy activeStrategy;

    public OrderQueueFactory(
            List<OrderQueueStrategy> strategyList,
            @Value("${awesome-pizza.order.queue-strategy:FIFO}") String activeStrategyName) {

        Map<String, OrderQueueStrategy> strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getStrategyName().toUpperCase(),
                        strategy -> strategy,
                        (first, second) -> {
                            throw new QueueStrategyConfigurationException(
                                    "Duplicate queue strategy name: " + first.getStrategyName().toUpperCase());
                        }
                ));

        this.activeStrategy = strategies.get(activeStrategyName.toUpperCase());
        if (this.activeStrategy == null) {
            throw new QueueStrategyConfigurationException(
                    "Queue strategy not supported: " + activeStrategyName + ". Available strategies: " + strategies.keySet());
        }
        log.info("Queue strategy: {}", this.activeStrategy.getStrategyName());
    }

    public OrderQueueStrategy getActiveStrategy() {
        return activeStrategy;
    }
}
