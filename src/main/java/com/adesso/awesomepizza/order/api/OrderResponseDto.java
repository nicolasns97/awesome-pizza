package com.adesso.awesomepizza.order.api;

import com.adesso.awesomepizza.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        String trackingCode,
        OrderStatus status,
        Instant createdAt,
        Instant startedAt,
        List<OrderItemResponseDto> items,
        BigDecimal total
) {}
