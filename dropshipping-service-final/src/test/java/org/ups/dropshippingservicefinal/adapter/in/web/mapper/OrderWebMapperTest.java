package org.ups.dropshippingservicefinal.adapter.in.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderDetail;
import org.ups.dropshippingservicefinal.adapter.in.web.generated.model.DropshippingOrderSummary;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderWebMapperTest {

    private OrderWebMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderWebMapper();
    }

    @Test
    void given_domainOrder_when_toSummary_then_allFieldsMapped() {
        DropshippingOrder order = buildOrder();

        DropshippingOrderSummary summary = mapper.toSummary(order);

        assertThat(summary.getOrderCode()).isEqualTo("ORD-001");
        assertThat(summary.getProductCode()).isEqualTo("PROD-001");
        assertThat(summary.getQuantity()).isEqualTo(3);
        assertThat(summary.getStatus().name()).isEqualTo("PENDING");
        assertThat(summary.getExpectedDeliveryDate()).isEqualTo(LocalDate.now().plusDays(10));
    }

    @Test
    void given_domainOrderWithHistory_when_toDetail_then_allFieldsMapped() {
        DropshippingOrder order = buildOrder();
        List<OrderStatusEvent> history = Collections.emptyList();

        DropshippingOrderDetail detail = mapper.toDetail(order, history);

        assertThat(detail.getOrderCode()).isEqualTo("ORD-001");
        assertThat(detail.getCustomerName()).isEqualTo("Cliente Test");
        assertThat(detail.getCustomerContact()).isEqualTo("cliente@email.com");
        assertThat(detail.getSpecialConditions()).isEqualTo("Condición especial");
        assertThat(detail.getDeliveryAddress().getStreet()).isEqualTo("Calle 1");
        assertThat(detail.getDeliveryAddress().getCity()).isEqualTo("Quito");
        assertThat(detail.getDeliveryAddress().getState()).isEqualTo("Pichincha");
        assertThat(detail.getDeliveryAddress().getCountry()).isEqualTo("Ecuador");
        assertThat(detail.getStatusHistory()).isEmpty();
    }

    private DropshippingOrder buildOrder() {
        Address address = new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador");
        return new DropshippingOrder(
                1L, "ORD-001", 42L, "PROD-001", "Producto Test",
                3, address, "Cliente Test", "cliente@email.com",
                LocalDate.now().plusDays(10), "Condición especial",
                OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
