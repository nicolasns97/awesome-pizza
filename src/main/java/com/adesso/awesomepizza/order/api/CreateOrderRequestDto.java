package com.adesso.awesomepizza.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequestDto(
        @NotEmpty(message = "The order must contain at least one pizza.")
        @Size(max = 20, message = "The order cannot contain more than 20 items.")
        List<@Valid OrderItemRequestDto> items
) {}
