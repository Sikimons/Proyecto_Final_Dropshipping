package org.ups.dropshippingservicefinal.adapter.out.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

@Component
public class InternalNotificationAdapter implements SendNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationAdapter.class);

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 100, multiplier = 2),
               recover = "recoverAccepted")
    @Override
    public void notifyOrderAccepted(OrderStatusEvent event) {
        doSend("ANALYST",
               String.format("[NOTIFY] ORDER_ACCEPTED orderId=%d actorId=%s estimatedDispatchDate=%s",
                             event.getOrderId(), event.getActorId(), event.getEstimatedDispatchDate()));
    }

    @Recover
    public void recoverAccepted(RuntimeException ex, OrderStatusEvent event) {
        log.error("[NOTIFICATION_FAILURE_EVENT] action=ACCEPTED orderId={} error={}",
                  event.getOrderId(), ex.getMessage());
    }

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 100, multiplier = 2),
               recover = "recoverRejected")
    @Override
    public void notifyOrderRejected(OrderStatusEvent event) {
        doSend("COMMERCIAL_TEAM",
               String.format("[ALERT_COMMERCIAL_TEAM] ORDER_REJECTED orderId=%d actorId=%s reason='%s'",
                             event.getOrderId(), event.getActorId(), event.getRejectionReason()));
    }

    @Recover
    public void recoverRejected(RuntimeException ex, OrderStatusEvent event) {
        log.error("[NOTIFICATION_FAILURE_EVENT] action=REJECTED orderId={} error={}",
                  event.getOrderId(), ex.getMessage());
    }

    void doSend(String channel, String message) {
        log.info("[CHANNEL={}] {}", channel, message);
    }
}
