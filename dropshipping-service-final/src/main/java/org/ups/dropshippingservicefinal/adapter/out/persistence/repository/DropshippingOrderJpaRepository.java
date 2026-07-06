package org.ups.dropshippingservicefinal.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.DropshippingOrderJpaEntity;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;

public interface DropshippingOrderJpaRepository
        extends JpaRepository<DropshippingOrderJpaEntity, Long> {

    List<DropshippingOrderJpaEntity> findByProviderId(Long providerId);

    List<DropshippingOrderJpaEntity> findByProviderIdAndStatus(Long providerId, OrderStatus status);
}
