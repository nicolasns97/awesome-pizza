package com.adesso.awesomepizza.order.api;

import com.adesso.awesomepizza.order.OrderEntity;
import com.adesso.awesomepizza.order.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponseDto toDto(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderResponseDto(
                entity.getTrackingCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getItems().stream().map(this::toDto).toList(),
                entity.getTotal()
        );
    }

    private OrderItemResponseDto toDto(OrderItemEntity item) {
        return new OrderItemResponseDto(
                item.getPizza().getId(),
                item.getPizza().getName(),
                item.getQuantity(),
                item.getNotes(),
                item.getUnitPrice()
        );
    }
}
