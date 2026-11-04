package com.adesso.awesomepizza.order.queue;

import com.adesso.awesomepizza.order.OrderEntity;
import com.adesso.awesomepizza.order.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class OrderQueueFactoryTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);

    @Test
    void selectsTheConfiguredStrategyIgnoringCase() {
        FifoQueueStrategy fifo = new FifoQueueStrategy(orderRepository);

        OrderQueueFactory factory = new OrderQueueFactory(List.of(fifo, priorityStrategy("CUSTOM")), "fifo");

        assertThat(factory.getActiveStrategy()).isSameAs(fifo);
    }

    @Test
    void failsFastOnUnknownStrategy() {
        assertThatThrownBy(() -> new OrderQueueFactory(List.of(new FifoQueueStrategy(orderRepository)), "UNKNOWN"))
                .isInstanceOf(QueueStrategyConfigurationException.class)
                .hasMessage("Queue strategy not supported: UNKNOWN. Available strategies: [FIFO]");
    }

    @Test
    void failsFastOnDuplicateStrategyNames() {
        assertThatThrownBy(() -> new OrderQueueFactory(List.of(new FifoQueueStrategy(orderRepository), priorityStrategy("fifo")), "FIFO"))
                .isInstanceOf(QueueStrategyConfigurationException.class)
                .hasMessage("Duplicate queue strategy name: FIFO");
    }

    private OrderQueueStrategy priorityStrategy(String name) {
        return new PriorityScoreQueueStrategy(orderRepository) {
            @Override
            public String getStrategyName() {
                return name;
            }

            @Override
            public int calculatePriorityScore(OrderEntity order) {
                return 0;
            }
        };
    }
}
