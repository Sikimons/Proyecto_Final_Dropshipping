package org.ups.dropshippingservicefinal.application.port.out;

import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

public interface SaveOrderStatusEventPort {

    OrderStatusEvent saveEvent(OrderStatusEvent event);
}
