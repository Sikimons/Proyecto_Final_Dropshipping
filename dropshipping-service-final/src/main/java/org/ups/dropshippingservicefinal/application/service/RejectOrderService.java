package org.ups.dropshippingservicefinal.application.service;

import org.ups.dropshippingservicefinal.application.port.in.RejectOrderUseCase;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.exception.InvalidRejectionReasonException;
import org.ups.dropshippingservicefinal.domain.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDateTime;

public class RejectOrderService implements RejectOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final SaveOrderStatusEventPort saveEventPort;
    private final SendNotificationPort notificationPort;

    public RejectOrderService(LoadOrderPort loadOrderPort,
                              SaveOrderPort saveOrderPort,
                              SaveOrderStatusEventPort saveEventPort,
                              SendNotificationPort notificationPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.saveEventPort = saveEventPort;
        this.notificationPort = notificationPort;
    }

    @Override
    public OrderStatusEvent reject(Long providerId, Long orderId, String reason) {
        DropshippingOrder order = loadOrderPort.loadOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "No se encontró la orden con ID " + orderId));

        if (!order.canTransitionTo(OrderStatus.REJECTED)) {
            throw new OrderAlreadyProcessedException(
                    "La orden " + order.getOrderCode() + " ya se encuentra en estado "
                            + order.getStatus() + " y no puede modificarse.");
        }

        if (reason == null || reason.isBlank()) {
            throw new InvalidRejectionReasonException(
                    "El motivo de rechazo es obligatorio y no puede estar vacío.");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.REJECTED);
        order.setUpdatedAt(LocalDateTime.now());
        saveOrderPort.saveOrder(order);

        OrderStatusEvent event = new OrderStatusEvent(
                null, orderId, previousStatus, OrderStatus.REJECTED,
                String.valueOf(providerId), LocalDateTime.now(),
                null, reason
        );
        OrderStatusEvent saved = saveEventPort.saveEvent(event);
        notificationPort.notifyOrderRejected(saved);
        return saved;
    }
}
