package org.ups.dropshippingservicefinal.adapter.out.persistence;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.DropshippingOrderJpaEntity;
import org.ups.dropshippingservicefinal.adapter.out.persistence.mapper.OrderPersistenceMapper;
import org.ups.dropshippingservicefinal.adapter.out.persistence.repository.DropshippingOrderJpaRepository;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DropshippingOrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final DropshippingOrderJpaRepository repository;
    private final OrderPersistenceMapper mapper;

    public DropshippingOrderPersistenceAdapter(DropshippingOrderJpaRepository repository,
                                               OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<DropshippingOrder> loadOrdersByProvider(Long providerId, OrderStatus statusFilter) {
        List<DropshippingOrderJpaEntity> entities = statusFilter != null
                ? repository.findByProviderIdAndStatus(providerId, statusFilter)
                : repository.findByProviderId(providerId);
        return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<DropshippingOrder> loadOrder(Long orderId) {
        return repository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public DropshippingOrder saveOrder(DropshippingOrder order) {
        DropshippingOrderJpaEntity entity = mapper.toJpaEntity(order);
        DropshippingOrderJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
