package com.adesso.awesomepizza.order.api;

import com.adesso.awesomepizza.auth.KitchenOnly;
import com.adesso.awesomepizza.order.OrderEntity;
import com.adesso.awesomepizza.order.OrderService;
import com.adesso.awesomepizza.order.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Set<OrderStatus> DEFAULT_LIST_STATUSES = EnumSet.of(OrderStatus.CREATED, OrderStatus.IN_PROGRESS);

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

    @Operation(summary = "List orders (kitchen)",
            description = "Paginated, in queue order. Defaults to CREATED and IN_PROGRESS; size is at most 100.")
    @KitchenOnly
    @GetMapping
    public ResponseEntity<OrderPageResponseDto> listOrders(
            @RequestParam(value = "status", required = false) Set<OrderStatus> statuses,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Set<OrderStatus> requestedStatuses = statuses == null || statuses.isEmpty() ? DEFAULT_LIST_STATUSES : statuses;
        Page<OrderEntity> orders = orderService.listOrders(requestedStatuses, PageRequest.of(page, size));
        return ResponseEntity.ok(new OrderPageResponseDto(
                orders.getContent().stream().map(orderMapper::toDto).toList(),
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()));
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
