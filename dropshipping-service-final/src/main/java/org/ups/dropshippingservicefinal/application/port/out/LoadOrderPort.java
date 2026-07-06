package org.ups.dropshippingservicefinal.application.port.out;

import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface LoadOrderPort {

    List<DropshippingOrder> loadOrdersByProvider(Long providerId, OrderStatus statusFilter);

    Optional<DropshippingOrder> loadOrder(Long orderId);
}
