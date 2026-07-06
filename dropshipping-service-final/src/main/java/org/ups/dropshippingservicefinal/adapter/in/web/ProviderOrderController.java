package org.ups.dropshippingservicefinal.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.ProviderOrdersApiDelegate;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.AcceptOrderRequest;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderDetail;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderSummary;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.OrderActionResponse;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.RejectOrderRequest;
import org.ups.dropshippingservicefinal.adapter.in.web.mapper.OrderWebMapper;
import org.ups.dropshippingservicefinal.application.port.in.AcceptOrderUseCase;
import org.ups.dropshippingservicefinal.application.port.in.GetOrderDetailUseCase;
import org.ups.dropshippingservicefinal.application.port.in.GetProviderOrdersUseCase;
import org.ups.dropshippingservicefinal.application.port.in.RejectOrderUseCase;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderEventsPort;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProviderOrderController implements ProviderOrdersApiDelegate {

    private final GetProviderOrdersUseCase getProviderOrdersUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final LoadOrderEventsPort loadOrderEventsPort;
    private final OrderWebMapper webMapper;

    public ProviderOrderController(GetProviderOrdersUseCase getProviderOrdersUseCase,
                                   GetOrderDetailUseCase getOrderDetailUseCase,
                                   AcceptOrderUseCase acceptOrderUseCase,
                                   RejectOrderUseCase rejectOrderUseCase,
                                   LoadOrderEventsPort loadOrderEventsPort,
                                   OrderWebMapper webMapper) {
        this.getProviderOrdersUseCase = getProviderOrdersUseCase;
        this.getOrderDetailUseCase = getOrderDetailUseCase;
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.loadOrderEventsPort = loadOrderEventsPort;
        this.webMapper = webMapper;
    }

    @Override
    public ResponseEntity<List<DropshippingOrderSummary>> getProviderOrders(
            Long providerId,
            org.ups.dropshippingservicefinal.adapter.in.web.generated.model.OrderStatus status) {
        OrderStatus statusFilter = status != null ? OrderStatus.valueOf(status.getValue()) : null;
        List<DropshippingOrder> orders = getProviderOrdersUseCase.getOrders(providerId, statusFilter);
        List<DropshippingOrderSummary> summaries = orders.stream()
                .map(webMapper::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @Override
    public ResponseEntity<DropshippingOrderDetail> getOrderDetail(Long providerId, Long orderId) {
        DropshippingOrder order = getOrderDetailUseCase.getOrder(providerId, orderId);
        List<OrderStatusEvent> history = loadOrderEventsPort.loadEventsByOrder(orderId);
        return ResponseEntity.ok(webMapper.toDetail(order, history));
    }

    @Override
    public ResponseEntity<OrderActionResponse> acceptOrder(
            Long providerId, Long orderId, AcceptOrderRequest body) {
        DropshippingOrder order = getOrderDetailUseCase.getOrder(providerId, orderId);
        OrderStatusEvent event = acceptOrderUseCase.accept(
                providerId, orderId, body.getEstimatedDispatchDate());
        OrderActionResponse response = webMapper.toActionResponse(event,
                "Orden aceptada exitosamente. Analista notificado.");
        response.setOrderId(orderId);
        response.setOrderCode(order.getOrderCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<OrderActionResponse> rejectOrder(
            Long providerId, Long orderId, RejectOrderRequest body) {
        DropshippingOrder order = getOrderDetailUseCase.getOrder(providerId, orderId);
        OrderStatusEvent event = rejectOrderUseCase.reject(providerId, orderId, body.getReason());
        OrderActionResponse response = webMapper.toActionResponse(event,
                "Orden rechazada. Equipo comercial notificado.");
        response.setOrderId(orderId);
        response.setOrderCode(order.getOrderCode());
        return ResponseEntity.ok(response);
    }
}
