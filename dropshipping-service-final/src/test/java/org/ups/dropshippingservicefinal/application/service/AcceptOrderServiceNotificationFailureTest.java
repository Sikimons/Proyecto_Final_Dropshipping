package org.ups.dropshippingservicefinal.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptOrderServiceNotificationFailureTest {

    @Mock
    private LoadOrderPort loadOrderPort;
    @Mock
    private SaveOrderPort saveOrderPort;
    @Mock
    private SaveOrderStatusEventPort saveEventPort;
    @Mock
    private SendNotificationPort notificationPort;

    private AcceptOrderService service;

    @BeforeEach
    void setUp() {
        service = new AcceptOrderService(loadOrderPort, saveOrderPort, saveEventPort, notificationPort);
    }

    @Test
    void given_notificationFails_when_accept_then_orderAndEventPersistedBeforeNotification() {
        DropshippingOrder pendingOrder = buildPendingOrder();
        OrderStatusEvent savedEvent = buildEvent();
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(pendingOrder));
        when(saveOrderPort.saveOrder(any())).thenReturn(pendingOrder);
        when(saveEventPort.saveEvent(any())).thenReturn(savedEvent);
        doThrow(new RuntimeException("Notification channel unavailable"))
                .when(notificationPort).notifyOrderAccepted(any());

        assertThrows(RuntimeException.class,
                () -> service.accept(42L, 1L, LocalDate.now().plusDays(1)));

        InOrder callOrder = inOrder(saveOrderPort, saveEventPort, notificationPort);
        callOrder.verify(saveOrderPort).saveOrder(any());
        callOrder.verify(saveEventPort).saveEvent(any());
        callOrder.verify(notificationPort).notifyOrderAccepted(any());
    }

    private DropshippingOrder buildPendingOrder() {
        Address address = new Address("Calle Test", "Quito", "Pichincha", "170501", "Ecuador");
        return new DropshippingOrder(
                1L, "ORD-FAIL-TEST", 42L, "PROD-001", "Producto Test",
                1, address, "Cliente Test", "test@email.com",
                LocalDate.now().plusDays(10), null,
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private OrderStatusEvent buildEvent() {
        return new OrderStatusEvent(
                1L, 1L, OrderStatus.PENDING, OrderStatus.ACCEPTED,
                "42", LocalDateTime.now(), LocalDate.now().plusDays(1), null
        );
    }
}
