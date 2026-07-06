package org.ups.dropshippingservicefinal.application.service;

import org.ups.dropshippingservicefinal.application.port.in.GetProviderOrdersUseCase;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.util.List;

public class GetProviderOrdersService implements GetProviderOrdersUseCase {

    private final LoadOrderPort loadOrderPort;

    public GetProviderOrdersService(LoadOrderPort loadOrderPort) {
        this.loadOrderPort = loadOrderPort;
    }

    @Override
    public List<DropshippingOrder> getOrders(Long providerId, OrderStatus statusFilter) {
        return loadOrderPort.loadOrdersByProvider(providerId, statusFilter);
    }
}
