package com.adesso.awesomepizza.order.api;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long pizzaId,
        String pizzaName,
        Integer quantity,
        String notes,
        BigDecimal unitPrice
) {}
