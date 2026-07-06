package org.ups.dropshippingservicefinal.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusEventTest {

    @Test
    void given_acceptedStatusWithDate_when_construct_then_eventCreated() {
        OrderStatusEvent event = new OrderStatusEvent(
                null, 1L, OrderStatus.PENDING, OrderStatus.ACCEPTED,
                "42", LocalDateTime.now(), LocalDate.now().plusDays(5), null
        );
        assertThat(event.getNewStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(event.getEstimatedDispatchDate()).isNotNull();
    }

    @Test
    void given_acceptedStatusWithNullDate_when_construct_then_throwsException() {
        assertThatThrownBy(() ->
                new OrderStatusEvent(
                        null, 1L, OrderStatus.PENDING, OrderStatus.ACCEPTED,
                        "42", LocalDateTime.now(), null, null
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estimatedDispatchDate");
    }

    @Test
    void given_rejectedStatusWithReason_when_construct_then_eventCreated() {
        OrderStatusEvent event = new OrderStatusEvent(
                null, 1L, OrderStatus.PENDING, OrderStatus.REJECTED,
                "42", LocalDateTime.now(), null, "Sin stock disponible"
        );
        assertThat(event.getNewStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(event.getRejectionReason()).isEqualTo("Sin stock disponible");
    }

    @Test
    void given_rejectedStatusWithBlankReason_when_construct_then_throwsException() {
        assertThatThrownBy(() ->
                new OrderStatusEvent(
                        null, 1L, OrderStatus.PENDING, OrderStatus.REJECTED,
                        "42", LocalDateTime.now(), null, "  "
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rejectionReason");
    }
}
