package com.adesso.awesomepizza.order;

import com.adesso.awesomepizza.menu.PizzaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pizza_id", nullable = false)
    private PizzaEntity pizza;

    @Column(nullable = false)
    private Integer quantity;

    private String notes;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItemEntity() {}

    public OrderItemEntity(PizzaEntity pizza, Integer quantity, String notes) {
        this.pizza = pizza;
        this.quantity = quantity;
        this.notes = notes;
        this.unitPrice = pizza.getPrice();
    }

    public Long getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    void setOrder(OrderEntity order) {
        this.order = order;
    }

    public PizzaEntity getPizza() {
        return pizza;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
