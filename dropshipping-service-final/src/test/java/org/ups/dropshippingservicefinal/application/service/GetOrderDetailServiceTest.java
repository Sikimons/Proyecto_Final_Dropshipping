package org.ups.dropshippingservicefinal.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderDetailServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;

    private GetOrderDetailService service;

    @BeforeEach
    void setUp() {
        service = new GetOrderDetailService(loadOrderPort);
    }

    @Test
    void given_ordenExistente_when_getOrder_then_retornaDetalle() {
        DropshippingOrder order = buildOrder(1L, 42L);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));

        DropshippingOrder result = service.getOrder(42L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProviderId()).isEqualTo(42L);
    }

    @Test
    void given_ordenInexistente_when_getOrder_then_lanzaOrderNotFoundException() {
        when(loadOrderPort.loadOrder(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(42L, 999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void given_ordenDeOtroProveedor_when_getOrder_then_lanzaOrderNotFoundException() {
        DropshippingOrder order = buildOrder(1L, 99L);
        when(loadOrderPort.loadOrder(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.getOrder(42L, 1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private DropshippingOrder buildOrder(Long id, Long providerId) {
        return new DropshippingOrder(
                id, "ORD-00" + id, providerId, "PROD-001", "Producto Test",
                2, new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador"),
                "Cliente Test", "cliente@email.com", LocalDate.now().plusDays(10),
                null, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
