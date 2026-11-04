package com.adesso.awesomepizza.order;

import com.adesso.awesomepizza.common.BaseAuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_status_created_at", columnList = "status, createdAt"),
        @Index(name = "idx_status_priority_created", columnList = "status, priorityScore, createdAt")
})
public class OrderEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    private Instant startedAt;
    private Instant completedAt;
    private Instant canceledAt;

    @Column(nullable = false)
    private Integer priorityScore = 0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {}

    public OrderEntity(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public Long getId() {
        return id;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Integer getPriorityScore() {
        return priorityScore;
    }

    public void assignPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public List<OrderItemEntity> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItemEntity item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void startPreparation() {
        transition(OrderStatus.CREATED, OrderStatus.IN_PROGRESS);
        this.startedAt = Instant.now();
    }

    public void completeOrder() {
        transition(OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED);
        this.completedAt = Instant.now();
    }

    public void cancelOrder() {
        transition(OrderStatus.CREATED, OrderStatus.CANCELED);
        this.canceledAt = Instant.now();
    }

    private void transition(OrderStatus requiredStatus, OrderStatus targetStatus) {
        if (this.status != requiredStatus) {
            throw new InvalidOrderTransitionException(this.trackingCode, this.status, targetStatus);
        }
        this.status = targetStatus;
    }
}
