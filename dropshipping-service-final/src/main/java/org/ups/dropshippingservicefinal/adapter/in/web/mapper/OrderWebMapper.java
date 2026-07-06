package org.ups.dropshippingservicefinal.adapter.in.web.mapper;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.AddressDto;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderDetail;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderSummary;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.OrderActionResponse;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.OrderStatus;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.OrderStatusEventDto;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderWebMapper {

    public DropshippingOrderSummary toSummary(DropshippingOrder order) {
        DropshippingOrderSummary summary = new DropshippingOrderSummary();
        summary.setOrderId(order.getId());
        summary.setOrderCode(order.getOrderCode());
        summary.setProductCode(order.getProductCode());
        summary.setProductDescription(order.getProductDescription());
        summary.setQuantity(order.getQuantity());
        summary.setStatus(OrderStatus.valueOf(order.getStatus().name()));
        summary.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        return summary;
    }

    public DropshippingOrderDetail toDetail(DropshippingOrder order, List<OrderStatusEvent> history) {
        DropshippingOrderDetail detail = new DropshippingOrderDetail();
        detail.setOrderId(order.getId());
        detail.setOrderCode(order.getOrderCode());
        detail.setProductCode(order.getProductCode());
        detail.setProductDescription(order.getProductDescription());
        detail.setQuantity(order.getQuantity());
        detail.setCustomerName(order.getCustomerName());
        detail.setCustomerContact(order.getCustomerContact());
        detail.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        detail.setSpecialConditions(order.getSpecialConditions());
        detail.setStatus(OrderStatus.valueOf(order.getStatus().name()));
        detail.setCreatedAt(order.getCreatedAt().atOffset(ZoneOffset.UTC));
        detail.setUpdatedAt(order.getUpdatedAt().atOffset(ZoneOffset.UTC));

        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(order.getDeliveryAddress().getStreet());
        addressDto.setCity(order.getDeliveryAddress().getCity());
        addressDto.setState(order.getDeliveryAddress().getState());
        addressDto.setPostalCode(order.getDeliveryAddress().getPostalCode());
        addressDto.setCountry(order.getDeliveryAddress().getCountry());
        detail.setDeliveryAddress(addressDto);

        List<OrderStatusEventDto> eventDtos = history.stream()
                .map(this::toEventDto)
                .collect(Collectors.toList());
        detail.setStatusHistory(eventDtos);
        return detail;
    }

    public OrderActionResponse toActionResponse(OrderStatusEvent event, String message) {
        OrderActionResponse response = new OrderActionResponse();
        response.setOrderId(event.getOrderId());
        response.setPreviousStatus(OrderStatus.valueOf(event.getPreviousStatus().name()));
        response.setNewStatus(OrderStatus.valueOf(event.getNewStatus().name()));
        response.setActorId(event.getActorId());
        response.setTimestamp(event.getTimestamp().atOffset(ZoneOffset.UTC));
        response.setEstimatedDispatchDate(event.getEstimatedDispatchDate());
        response.setRejectionReason(event.getRejectionReason());
        response.setMessage(message);
        return response;
    }

    private OrderStatusEventDto toEventDto(OrderStatusEvent event) {
        OrderStatusEventDto dto = new OrderStatusEventDto();
        dto.setEventId(event.getId());
        dto.setPreviousStatus(OrderStatus.valueOf(event.getPreviousStatus().name()));
        dto.setNewStatus(OrderStatus.valueOf(event.getNewStatus().name()));
        dto.setActorId(event.getActorId());
        dto.setTimestamp(event.getTimestamp() != null
                ? event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));
        dto.setEstimatedDispatchDate(event.getEstimatedDispatchDate());
        dto.setRejectionReason(event.getRejectionReason());
        return dto;
    }
}
