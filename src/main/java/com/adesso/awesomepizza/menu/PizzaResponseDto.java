package com.adesso.awesomepizza.menu;

import java.math.BigDecimal;

public record PizzaResponseDto(
        Long id,
        String name,
        BigDecimal price
) {}
