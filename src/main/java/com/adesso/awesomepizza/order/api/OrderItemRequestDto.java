package com.adesso.awesomepizza.order.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderItemRequestDto(
        @NotNull(message = "The pizza id is required.")
        Long pizzaId,

        @NotNull(message = "The quantity is required.")
        @Min(value = 1, message = "The quantity must be at least 1.")
        @Max(value = 20, message = "The quantity cannot exceed 20.")
        Integer quantity,

        @Size(max = 255, message = "Notes cannot exceed 255 characters.")
        String notes
) {}
