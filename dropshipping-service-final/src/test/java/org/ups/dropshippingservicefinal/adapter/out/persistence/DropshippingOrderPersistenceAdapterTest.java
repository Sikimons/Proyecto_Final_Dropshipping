package org.ups.dropshippingservicefinal.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.ups.dropshippingservicefinal.adapter.out.persistence.mapper.OrderPersistenceMapper;
import org.ups.dropshippingservicefinal.adapter.out.persistence.repository.DropshippingOrderJpaRepository;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DropshippingOrderPersistenceAdapter.class, OrderPersistenceMapper.class})
class DropshippingOrderPersistenceAdapterTest {

    @Autowired
    private DropshippingOrderPersistenceAdapter adapter;

    @Test
    void given_seedData_when_findByProviderId_then_returnsAllOrders() {
        List<DropshippingOrder> orders = adapter.loadOrdersByProvider(42L, null);
        assertThat(orders).hasSize(3);
    }

    @Test
    void given_seedData_when_findByProviderIdAndStatus_then_returnsFiltered() {
        List<DropshippingOrder> pending = adapter.loadOrdersByProvider(42L, OrderStatus.PENDING);
        assertThat(pending).hasSize(2);
        assertThat(pending).allMatch(o -> o.getStatus() == OrderStatus.PENDING);
    }

    @Test
    void given_unknownId_when_loadOrder_then_returnsEmpty() {
        Optional<DropshippingOrder> result = adapter.loadOrder(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void given_existingId_when_loadOrder_then_returnsOrder() {
        List<DropshippingOrder> all = adapter.loadOrdersByProvider(42L, null);
        Long firstId = all.get(0).getId();

        Optional<DropshippingOrder> result = adapter.loadOrder(firstId);
        assertThat(result).isPresent();
    }
}
