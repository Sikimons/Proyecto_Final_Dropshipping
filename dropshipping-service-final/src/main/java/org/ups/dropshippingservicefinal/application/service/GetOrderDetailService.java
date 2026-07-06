package org.ups.dropshippingservicefinal.application.service;

import org.ups.dropshippingservicefinal.application.port.in.GetOrderDetailUseCase;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;
import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;

public class GetOrderDetailService implements GetOrderDetailUseCase {

    private final LoadOrderPort loadOrderPort;

    public GetOrderDetailService(LoadOrderPort loadOrderPort) {
        this.loadOrderPort = loadOrderPort;
    }

    @Override
    public DropshippingOrder getOrder(Long providerId, Long orderId) {
        DropshippingOrder order = loadOrderPort.loadOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "No se encontró la orden con ID " + orderId + " para el proveedor " + providerId));
        if (!order.getProviderId().equals(providerId)) {
            throw new OrderNotFoundException(
                    "No se encontró la orden con ID " + orderId + " para el proveedor " + providerId);
        }
        return order;
    }
}
