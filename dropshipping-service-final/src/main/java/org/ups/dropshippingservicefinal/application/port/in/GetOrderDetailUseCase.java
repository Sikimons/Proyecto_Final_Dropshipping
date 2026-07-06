package org.ups.dropshippingservicefinal.application.port.in;

import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;

public interface GetOrderDetailUseCase {

    DropshippingOrder getOrder(Long providerId, Long orderId);
}
