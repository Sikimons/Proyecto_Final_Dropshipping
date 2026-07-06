package org.ups.dropshippingservicefinal.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.DropshippingOrderJpaEntity;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.OrderStatusEventJpaEntity;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

@Component
public class OrderPersistenceMapper {

    public DropshippingOrder toDomain(DropshippingOrderJpaEntity entity) {
        Address address = new Address(
                entity.getStreet(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry()
        );
        return new DropshippingOrder(
                entity.getId(),
                entity.getOrderCode(),
                entity.getProviderId(),
                entity.getProductCode(),
                entity.getProductDescription(),
                entity.getQuantity(),
                address,
                entity.getCustomerName(),
                entity.getCustomerContact(),
                entity.getExpectedDeliveryDate(),
                entity.getSpecialConditions(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public DropshippingOrderJpaEntity toJpaEntity(DropshippingOrder order) {
        return DropshippingOrderJpaEntity.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .providerId(order.getProviderId())
                .productCode(order.getProductCode())
                .productDescription(order.getProductDescription())
                .quantity(order.getQuantity())
                .street(order.getDeliveryAddress().getStreet())
                .city(order.getDeliveryAddress().getCity())
                .state(order.getDeliveryAddress().getState())
                .postalCode(order.getDeliveryAddress().getPostalCode())
                .country(order.getDeliveryAddress().getCountry())
                .customerName(order.getCustomerName())
                .customerContact(order.getCustomerContact())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .specialConditions(order.getSpecialConditions())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderStatusEvent toEventDomain(OrderStatusEventJpaEntity entity) {
        return new OrderStatusEvent(
                entity.getId(),
                entity.getOrder().getId(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getActorId(),
                entity.getTimestamp(),
                entity.getEstimatedDispatchDate(),
                entity.getRejectionReason()
        );
    }

    public OrderStatusEventJpaEntity toEventJpaEntity(OrderStatusEvent event,
                                                       DropshippingOrderJpaEntity orderEntity) {
        return OrderStatusEventJpaEntity.builder()
                .id(event.getId())
                .order(orderEntity)
                .previousStatus(event.getPreviousStatus())
                .newStatus(event.getNewStatus())
                .actorId(event.getActorId())
                .timestamp(event.getTimestamp())
                .estimatedDispatchDate(event.getEstimatedDispatchDate())
                .rejectionReason(event.getRejectionReason())
                .build();
    }
}
