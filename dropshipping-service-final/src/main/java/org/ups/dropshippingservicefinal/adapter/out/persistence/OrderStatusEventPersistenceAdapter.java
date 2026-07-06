package org.ups.dropshippingservicefinal.adapter.out.persistence;

import org.springframework.stereotype.Component;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.DropshippingOrderJpaEntity;
import org.ups.dropshippingservicefinal.adapter.out.persistence.entity.OrderStatusEventJpaEntity;
import org.ups.dropshippingservicefinal.adapter.out.persistence.mapper.OrderPersistenceMapper;
import org.ups.dropshippingservicefinal.adapter.out.persistence.repository.DropshippingOrderJpaRepository;
import org.ups.dropshippingservicefinal.adapter.out.persistence.repository.OrderStatusEventJpaRepository;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderEventsPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderStatusEventPersistenceAdapter implements SaveOrderStatusEventPort, LoadOrderEventsPort {

    private final OrderStatusEventJpaRepository eventRepository;
    private final DropshippingOrderJpaRepository orderRepository;
    private final OrderPersistenceMapper mapper;

    public OrderStatusEventPersistenceAdapter(OrderStatusEventJpaRepository eventRepository,
                                              DropshippingOrderJpaRepository orderRepository,
                                              OrderPersistenceMapper mapper) {
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Override
    public List<OrderStatusEvent> loadEventsByOrder(Long orderId) {
        return eventRepository.findByOrderIdOrderByTimestampAsc(orderId)
                .stream()
                .map(mapper::toEventDomain)
                .collect(Collectors.toList());
    }

    @Override
    public OrderStatusEvent saveEvent(OrderStatusEvent event) {
        DropshippingOrderJpaEntity orderEntity = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "No se encontró la orden con ID " + event.getOrderId()));
        OrderStatusEventJpaEntity entity = mapper.toEventJpaEntity(event, orderEntity);
        OrderStatusEventJpaEntity saved = eventRepository.save(entity);
        return mapper.toEventDomain(saved);
    }
}
