package com.adesso.awesomepizza.order.api;

import com.adesso.awesomepizza.auth.KitchenOnly;
import com.adesso.awesomepizza.order.OrderService;
import com.adesso.awesomepizza.order.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @Operation(summary = "Place an order", description = "Returns the tracking code the customer uses to follow or cancel the order.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto request) {
        OrderResponseDto response = orderMapper.toDto(orderService.createOrder(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get an order by tracking code")
    @GetMapping("/{trackingCode}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable String trackingCode) {
        OrderResponseDto response = orderMapper.toDto(orderService.getOrder(trackingCode));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel an order", description = "Only possible while the order has not been taken by the kitchen.")
    @PostMapping("/{trackingCode}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable String trackingCode) {
        OrderResponseDto response = orderMapper.toDto(orderService.cancelOrder(trackingCode));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List orders (kitchen)", description = "Orders in queue order. Defaults to CREATED and IN_PROGRESS.")
    @KitchenOnly
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam(value = "status", required = false) Set<OrderStatus> statuses) {
        Set<OrderStatus> requestedStatuses = statuses == null || statuses.isEmpty()
                ? EnumSet.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS)
                : statuses;
        List<OrderResponseDto> orders = orderService.listOrders(requestedStatuses).stream()
                .map(orderMapper::toDto)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Take the next order (kitchen)", description = "Moves the next queued order to IN_PROGRESS.")
    @ApiResponse(responseCode = "200", description = "Order taken")
    @ApiResponse(responseCode = "204", description = "Queue is empty", content = @Content)
    @KitchenOnly
    @PostMapping("/next")
    public ResponseEntity<OrderResponseDto> takeNextOrder() {
        return orderService.takeNextOrder()
                .map(orderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Complete an order (kitchen)")
    @KitchenOnly
    @PostMapping("/{trackingCode}/complete")
    public ResponseEntity<OrderResponseDto> completeOrder(@PathVariable String trackingCode) {
        OrderResponseDto response = orderMapper.toDto(orderService.completeOrder(trackingCode));
        return ResponseEntity.ok(response);
    }
}
