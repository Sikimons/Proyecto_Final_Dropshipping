package org.ups.dropshippingservicefinal.application.port.out;

import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

public interface SendNotificationPort {

    void notifyOrderAccepted(OrderStatusEvent event);

    void notifyOrderRejected(OrderStatusEvent event);
}
