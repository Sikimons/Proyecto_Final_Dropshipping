package org.ups.dropshippingservicefinal.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DropshippingOrderTest {

    private DropshippingOrder buildOrder(OrderStatus status) {
        return new DropshippingOrder(
                1L, "ORD-001", 42L, "PROD-001", "Producto Test",
                3, new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador"),
                "Cliente Test", "cliente@email.com", LocalDate.now().plusDays(10),
                null, status, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void given_validFields_when_construct_then_entityCreated() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        assertThat(order.getOrderCode()).isEqualTo("ORD-001");
        assertThat(order.getQuantity()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void given_zeroQuantity_when_construct_then_throwsException() {
        assertThatThrownBy(() ->
                new DropshippingOrder(
                        1L, "ORD-001", 42L, "PROD-001", "Producto Test",
                        0, new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador"),
                        "Cliente Test", "cliente@email.com", LocalDate.now().plusDays(10),
                        null, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_pendingOrder_when_canTransitionToAccepted_then_returnsTrue() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        assertThat(order.canTransitionTo(OrderStatus.ACCEPTED)).isTrue();
    }

    @Test
    void given_pendingOrder_when_canTransitionToRejected_then_returnsTrue() {
        DropshippingOrder order = buildOrder(OrderStatus.PENDING);
        assertThat(order.canTransitionTo(OrderStatus.REJECTED)).isTrue();
    }

    @Test
    void given_acceptedOrder_when_canTransitionToAccepted_then_returnsFalse() {
        DropshippingOrder order = buildOrder(OrderStatus.ACCEPTED);
        assertThat(order.canTransitionTo(OrderStatus.ACCEPTED)).isFalse();
    }

    @Test
    void given_rejectedOrder_when_canTransitionToAccepted_then_returnsFalse() {
        DropshippingOrder order = buildOrder(OrderStatus.REJECTED);
        assertThat(order.canTransitionTo(OrderStatus.ACCEPTED)).isFalse();
    }
}
