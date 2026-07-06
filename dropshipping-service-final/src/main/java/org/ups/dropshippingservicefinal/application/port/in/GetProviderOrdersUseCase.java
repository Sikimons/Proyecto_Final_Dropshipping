package org.ups.dropshippingservicefinal.application.port.in;

import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;

public interface GetProviderOrdersUseCase {

    List<DropshippingOrder> getOrders(Long providerId, OrderStatus statusFilter);
}
