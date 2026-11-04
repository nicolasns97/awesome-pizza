package com.adesso.awesomepizza.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderEntityTest {

    @Test
    void followsTheHappyPath() {
        OrderEntity order = new OrderEntity("ORD-1");

        order.startPreparation();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(order.getStartedAt()).isNotNull();

        order.completeOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void canBeCanceledWhileCreated() {
        OrderEntity order = new OrderEntity("ORD-1");

        order.cancelOrder();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCanceledAt()).isNotNull();
    }

    @Test
    void cannotBeCompletedBeforePreparation() {
        OrderEntity order = new OrderEntity("ORD-1");

        assertThatThrownBy(order::completeOrder)
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessage("Order ORD-1 cannot move from CREATED to COMPLETED.");
    }

    @Test
    void cannotBeStartedTwice() {
        OrderEntity order = new OrderEntity("ORD-1");
        order.startPreparation();

        assertThatThrownBy(order::startPreparation)
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessage("Order ORD-1 cannot move from IN_PROGRESS to IN_PROGRESS.");
    }

    @Test
    void cannotBeCanceledOncePreparationStarted() {
        OrderEntity order = new OrderEntity("ORD-1");
        order.startPreparation();

        assertThatThrownBy(order::cancelOrder)
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessage("Order ORD-1 cannot move from IN_PROGRESS to CANCELED.");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }
}
