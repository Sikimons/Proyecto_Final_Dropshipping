package org.ups.dropshippingservicefinal.application.port.in;

import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

public interface RejectOrderUseCase {

    OrderStatusEvent reject(Long providerId, Long orderId, String reason);
}
