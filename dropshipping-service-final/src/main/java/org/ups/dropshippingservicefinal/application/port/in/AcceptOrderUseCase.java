package org.ups.dropshippingservicefinal.application.port.in;

import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;

public interface AcceptOrderUseCase {

    OrderStatusEvent accept(Long providerId, Long orderId, LocalDate estimatedDispatchDate);
}
