package org.ups.dropshippingservicefinal.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.domain.model.Address;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProviderOrdersServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;

    private GetProviderOrdersService service;

    @BeforeEach
    void setUp() {
        service = new GetProviderOrdersService(loadOrderPort);
    }

    @Test
    void given_proveedorConOrdenes_when_getOrders_then_retornaListaCompleta() {
        List<DropshippingOrder> orders = List.of(buildOrder(1L, OrderStatus.PENDING));
        when(loadOrderPort.loadOrdersByProvider(42L, null)).thenReturn(orders);

        List<DropshippingOrder> result = service.getOrders(42L, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProviderId()).isEqualTo(42L);
    }

    @Test
    void given_proveedorSinOrdenes_when_getOrders_then_retornaListaVacia() {
        when(loadOrderPort.loadOrdersByProvider(99L, null)).thenReturn(Collections.emptyList());

        List<DropshippingOrder> result = service.getOrders(99L, null);

        assertThat(result).isEmpty();
    }

    @Test
    void given_filtroEstado_when_getOrders_then_retornaFiltrado() {
        List<DropshippingOrder> pendingOrders = List.of(buildOrder(1L, OrderStatus.PENDING));
        when(loadOrderPort.loadOrdersByProvider(42L, OrderStatus.PENDING)).thenReturn(pendingOrders);

        List<DropshippingOrder> result = service.getOrders(42L, OrderStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    private DropshippingOrder buildOrder(Long id, OrderStatus status) {
        return new DropshippingOrder(
                id, "ORD-00" + id, 42L, "PROD-001", "Producto Test",
                2, new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador"),
                "Cliente Test", "cliente@email.com", LocalDate.now().plusDays(10),
                null, status, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
