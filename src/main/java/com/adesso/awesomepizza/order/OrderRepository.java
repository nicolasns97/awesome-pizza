package com.adesso.awesomepizza.order;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    String SKIP_LOCKED = "-2";
    String LOCK_TIMEOUT_MILLIS = "3000";

    @EntityGraph(attributePaths = {"items", "items.pizza"})
    Optional<OrderEntity> findWithItemsByTrackingCode(String trackingCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS)})
    Optional<OrderEntity> findWithLockByTrackingCode(String trackingCode);

    @Query(value = "SELECT o.id FROM OrderEntity o WHERE o.status IN :statuses ORDER BY o.priorityScore DESC, o.createdAt ASC, o.id ASC",
            countQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.status IN :statuses")
    Page<Long> findIdsInQueueOrder(@Param("statuses") Collection<OrderStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.pizza"})
    @Query("SELECT o FROM OrderEntity o WHERE o.id IN :ids ORDER BY o.priorityScore DESC, o.createdAt ASC, o.id ASC")
    List<OrderEntity> findWithItemsByIdInQueueOrder(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = SKIP_LOCKED)})
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status ORDER BY o.createdAt ASC, o.id ASC LIMIT 1")
    Optional<OrderEntity> findNextFifoOrder(@Param("status") OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = SKIP_LOCKED)})
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status ORDER BY o.priorityScore DESC, o.createdAt ASC, o.id ASC LIMIT 1")
    Optional<OrderEntity> findNextPriorityOrder(@Param("status") OrderStatus status);
}
