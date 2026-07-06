package org.ups.dropshippingservicefinal.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.OrderStatusEventJpaEntity;

import java.util.List;

public interface OrderStatusEventJpaRepository
        extends JpaRepository<OrderStatusEventJpaEntity, Long> {

    List<OrderStatusEventJpaEntity> findByOrderIdOrderByTimestampAsc(Long orderId);
}
