package org.ups.dropshippingservicefinal.application.service;

import org.ups.dropshippingservicefinal.application.port.in.AcceptOrderUseCase;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.exception.InvalidDispatchDateException;
import org.ups.dropshippingservicefinal.domain.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AcceptOrderService implements AcceptOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final SaveOrderStatusEventPort saveEventPort;
    private final SendNotificationPort notificationPort;

    public AcceptOrderService(LoadOrderPort loadOrderPort,
                              SaveOrderPort saveOrderPort,
                              SaveOrderStatusEventPort saveEventPort,
                              SendNotificationPort notificationPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.saveEventPort = saveEventPort;
        this.notificationPort = notificationPort;
    }

    @Override
    public OrderStatusEvent accept(Long providerId, Long orderId, LocalDate estimatedDispatchDate) {
        DropshippingOrder order = loadOrderPort.loadOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "No se encontró la orden con ID " + orderId));

        if (!order.canTransitionTo(OrderStatus.ACCEPTED)) {
            throw new OrderAlreadyProcessedException(
                    "La orden " + order.getOrderCode() + " ya se encuentra en estado "
                            + order.getStatus() + " y no puede modificarse.");
        }

        if (!estimatedDispatchDate.isAfter(LocalDate.now())) {
            throw new InvalidDispatchDateException(
                    "La fecha estimada de despacho debe ser posterior a la fecha actual.");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());
        saveOrderPort.saveOrder(order);

        OrderStatusEvent event = new OrderStatusEvent(
                null, orderId, previousStatus, OrderStatus.ACCEPTED,
                String.valueOf(providerId), LocalDateTime.now(),
                estimatedDispatchDate, null
        );
        OrderStatusEvent saved = saveEventPort.saveEvent(event);
        notificationPort.notifyOrderAccepted(saved);
        return saved;
    }
}
