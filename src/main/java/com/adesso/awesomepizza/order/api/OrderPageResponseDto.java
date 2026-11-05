package com.adesso.awesomepizza.order.api;

import java.util.List;

public record OrderPageResponseDto(
        List<OrderResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
