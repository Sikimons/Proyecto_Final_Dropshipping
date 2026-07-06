package org.ups.dropshippingservicefinal.adapter.out.persistence.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.DropshippingOrderJpaEntity;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.OrderStatusEventJpaEntity;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPersistenceMapperTest {

    private OrderPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderPersistenceMapper();
    }

    @Test
    void given_domainOrder_when_roundtrip_then_noFieldLost() {
        Address address = new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador");
        DropshippingOrder original = new DropshippingOrder(
                1L, "ORD-001", 42L, "PROD-001", "Producto Test",
                3, address, "Cliente Test", "cliente@email.com",
                LocalDate.now().plusDays(10), "Condición especial",
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()
        );

        DropshippingOrderJpaEntity entity = mapper.toJpaEntity(original);
        DropshippingOrder restored = mapper.toDomain(entity);

        assertThat(restored.getOrderCode()).isEqualTo(original.getOrderCode());
        assertThat(restored.getProviderId()).isEqualTo(original.getProviderId());
        assertThat(restored.getProductCode()).isEqualTo(original.getProductCode());
        assertThat(restored.getQuantity()).isEqualTo(original.getQuantity());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getDeliveryAddress().getStreet()).isEqualTo(address.getStreet());
        assertThat(restored.getDeliveryAddress().getCity()).isEqualTo(address.getCity());
        assertThat(restored.getDeliveryAddress().getCountry()).isEqualTo(address.getCountry());
        assertThat(restored.getSpecialConditions()).isEqualTo(original.getSpecialConditions());
    }

    @Test
    void given_eventWithDispatchDate_when_roundtrip_then_dispatchDatePreserved() {
        LocalDate dispatchDate = LocalDate.now().plusDays(5);
        DropshippingOrderJpaEntity orderEntity = new DropshippingOrderJpaEntity();
        orderEntity.setId(1L);

        OrderStatusEventJpaEntity eventEntity = OrderStatusEventJpaEntity.builder()
                .id(10L)
                .order(orderEntity)
                .previousStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.ACCEPTED)
                .actorId("42")
                .timestamp(LocalDateTime.now())
                .estimatedDispatchDate(dispatchDate)
                .build();

        OrderStatusEvent domain = mapper.toEventDomain(eventEntity);

        assertThat(domain.getNewStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(domain.getEstimatedDispatchDate()).isEqualTo(dispatchDate);
        assertThat(domain.getRejectionReason()).isNull();
    }

    @Test
    void given_eventWithRejectionReason_when_roundtrip_then_reasonPreserved() {
        DropshippingOrderJpaEntity orderEntity = new DropshippingOrderJpaEntity();
        orderEntity.setId(1L);

        OrderStatusEventJpaEntity eventEntity = OrderStatusEventJpaEntity.builder()
                .id(11L)
                .order(orderEntity)
                .previousStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.REJECTED)
                .actorId("42")
                .timestamp(LocalDateTime.now())
                .rejectionReason("Sin stock disponible")
                .build();

        OrderStatusEvent domain = mapper.toEventDomain(eventEntity);

        assertThat(domain.getNewStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(domain.getRejectionReason()).isEqualTo("Sin stock disponible");
        assertThat(domain.getEstimatedDispatchDate()).isNull();
    }
}
