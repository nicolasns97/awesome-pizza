package com.adesso.awesomepizza.order;

import com.adesso.awesomepizza.menu.PizzaEntity;
import com.adesso.awesomepizza.menu.PizzaNotAvailableException;
import com.adesso.awesomepizza.menu.PizzaRepository;
import com.adesso.awesomepizza.order.api.CreateOrderRequestDto;
import com.adesso.awesomepizza.order.api.OrderItemRequestDto;
import com.adesso.awesomepizza.order.queue.OrderQueueFactory;
import com.adesso.awesomepizza.order.queue.OrderQueueStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String TRACKING_CODE = "ORD-TEST-0000-0001";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PizzaRepository pizzaRepository;

    @Mock
    private OrderQueueFactory queueFactory;

    @Mock
    private OrderQueueStrategy queueStrategy;

    @Mock
    private TrackingCodeGenerator trackingCodeGenerator;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void useMockStrategy() {
        lenient().when(queueFactory.getActiveStrategy()).thenReturn(queueStrategy);
    }

    @Test
    void createOrderAddsItemsAtMenuPriceAndAssignsPriorityScore() {
        PizzaEntity margherita = new PizzaEntity("Margherita", new BigDecimal("6.50"));
        when(trackingCodeGenerator.generate()).thenReturn(TRACKING_CODE);
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));
        when(queueStrategy.calculatePriorityScore(any())).thenReturn(7);
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity order = orderService.createOrder(request(new OrderItemRequestDto(1L, 2, "well done")));

        assertThat(order.getTrackingCode()).isEqualTo(TRACKING_CODE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getPriorityScore()).isEqualTo(7);
        assertThat(order.getTotal()).isEqualByComparingTo("13.00");
        assertThat(order.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getPizza()).isSameAs(margherita);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getNotes()).isEqualTo("well done");
        });
    }

    @Test
    void createOrderRejectsUnavailablePizza() {
        PizzaEntity bufala = new PizzaEntity("Bufala", new BigDecimal("9.50"));
        bufala.setAvailable(false);
        when(pizzaRepository.findById(6L)).thenReturn(Optional.of(bufala));

        assertThatThrownBy(() -> orderService.createOrder(request(new OrderItemRequestDto(6L, 1, null))))
                .isInstanceOf(PizzaNotAvailableException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderRejectsUnknownPizza() {
        when(pizzaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request(new OrderItemRequestDto(999L, 1, null))))
                .isInstanceOf(PizzaNotAvailableException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void takeNextOrderStartsPreparation() {
        OrderEntity order = createdOrder();
        when(queueStrategy.fetchNextOrder()).thenReturn(Optional.of(order));
        when(orderRepository.findWithItemsByTrackingCode(TRACKING_CODE)).thenReturn(Optional.of(order));

        Optional<OrderEntity> taken = orderService.takeNextOrder();

        assertThat(taken).containsSame(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void takeNextOrderReturnsEmptyWhenQueueIsEmpty() {
        when(queueStrategy.fetchNextOrder()).thenReturn(Optional.empty());

        assertThat(orderService.takeNextOrder()).isEmpty();
    }

    @Test
    void completeOrderCompletesAnOrderInPreparation() {
        OrderEntity order = createdOrder();
        order.startPreparation();
        lockAndReload(order);

        assertThat(orderService.completeOrder(TRACKING_CODE).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void cancelOrderCancelsACreatedOrder() {
        OrderEntity order = createdOrder();
        lockAndReload(order);

        assertThat(orderService.cancelOrder(TRACKING_CODE).getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void cancelOrderFailsOncePreparationStarted() {
        OrderEntity order = createdOrder();
        order.startPreparation();
        when(orderRepository.findWithLockByTrackingCode(TRACKING_CODE)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(TRACKING_CODE))
                .isInstanceOf(InvalidOrderTransitionException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void lockTimeoutIsReportedAsOrderLocked() {
        when(orderRepository.findWithLockByTrackingCode(TRACKING_CODE))
                .thenThrow(new PessimisticLockingFailureException("lock timeout"));

        assertThatThrownBy(() -> orderService.cancelOrder(TRACKING_CODE))
                .isInstanceOf(OrderLockedException.class);
    }

    @Test
    void unknownTrackingCodeIsReportedAsNotFound() {
        when(orderRepository.findWithLockByTrackingCode(TRACKING_CODE)).thenReturn(Optional.empty());
        when(orderRepository.findWithItemsByTrackingCode(TRACKING_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.completeOrder(TRACKING_CODE)).isInstanceOf(OrderNotFoundException.class);
        assertThatThrownBy(() -> orderService.getOrder(TRACKING_CODE)).isInstanceOf(OrderNotFoundException.class);
    }

    private static CreateOrderRequestDto request(OrderItemRequestDto... items) {
        return new CreateOrderRequestDto(List.of(items));
    }

    private static OrderEntity createdOrder() {
        OrderEntity order = new OrderEntity(TRACKING_CODE);
        ReflectionTestUtils.setField(order, "createdAt", Instant.now());
        return order;
    }

    private void lockAndReload(OrderEntity order) {
        when(orderRepository.findWithLockByTrackingCode(TRACKING_CODE)).thenReturn(Optional.of(order));
        when(orderRepository.findWithItemsByTrackingCode(TRACKING_CODE)).thenReturn(Optional.of(order));
    }
}
