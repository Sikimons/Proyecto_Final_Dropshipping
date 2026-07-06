package org.ups.dropshippingservicefinal.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.exception.InvalidRejectionReasonException;
import org.ups.dropshippingservicefinal.domain.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectOrderServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;
    @Mock
    private SaveOrderPort saveOrderPort;
    @Mock
    private SaveOrderStatusEventPort saveEventPort;
    @Mock
    private SendNotificationPort notificationPort;

    private RejectOrderService service;

    @BeforeEach
    void setUp() {
        service = new RejectOrderService(loadOrderPort, saveOrderPort, saveEventPort, notificationPort);
    }

    @Test
    void given_ordenPendiente_when_reject_then_cambiaARejected() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));
        when(saveOrderPort.saveOrder(any())).thenReturn(order);
        when(saveEventPort.saveEvent(any())).thenReturn(buildEvent());

        OrderStatusEvent result = service.reject(42L, 1L, "Sin stock disponible");

        assertThat(result).isNotNull();
    }

    @Test
    void given_ordenYaRechazada_when_reject_then_lanzaOrderAlreadyProcessedException() {
        DropshippingOrder order = buildOrder(OrderStatus.REJECTED);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.reject(42L, 1L, "Motivo"))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    @Test
    void given_motivoVacio_when_reject_then_lanzaInvalidRejectionReasonException() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.reject(42L, 1L, "  "))
                .isInstanceOf(InvalidRejectionReasonException.class);
    }

    @Test
    void given_ordenPendiente_when_reject_then_llamaNotifyOrderRejected() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));
        when(saveOrderPort.saveOrder(any())).thenReturn(order);
        when(saveEventPort.saveEvent(any())).thenReturn(buildEvent());

        service.reject(42L, 1L, "Sin stock disponible");

        verify(notificationPort).notifyOrderRejected(any());
    }

    @Test
    void given_ordenNoEncontrada_when_reject_then_lanzaOrderNotFoundException() {
        when(loadOrderPort.loadOrder(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(42L, 999L, "Motivo"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private DropshippingOrder buildOrder(OrderStatus status) {
        return new DropshippingOrder(
                1L, "ORD-001", 42L, "PROD-001", "Producto Test",
                2, new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador"),
                "Cliente Test", "cliente@email.com", LocalDate.now().plusDays(10),
                null, status, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private OrderStatusEvent buildEvent() {
        return new OrderStatusEvent(
                1L, 1L, OrderStatus.PENDING, OrderStatus.REJECTED,
                "42", LocalDateTime.now(), null, "Sin stock disponible"
        );
    }
}
