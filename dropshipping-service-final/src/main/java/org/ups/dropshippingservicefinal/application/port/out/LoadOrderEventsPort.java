package org.ups.dropshippingservicefinal.application.port.out;

import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.util.List;

public interface LoadOrderEventsPort {

    List<OrderStatusEvent> loadEventsByOrder(Long orderId);
}
