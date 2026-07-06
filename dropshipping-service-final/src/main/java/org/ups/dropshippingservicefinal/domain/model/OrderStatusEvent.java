package org.ups.dropshippingservicefinal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderStatusEvent {

    private final Long id;
    private final Long orderId;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final String actorId;
    private final LocalDateTime timestamp;
    private final LocalDate estimatedDispatchDate;
    private final String rejectionReason;

    public OrderStatusEvent(Long id, Long orderId, OrderStatus previousStatus,
                            OrderStatus newStatus, String actorId, LocalDateTime timestamp,
                            LocalDate estimatedDispatchDate, String rejectionReason) {
        if (newStatus == OrderStatus.ACCEPTED && estimatedDispatchDate == null) {
            throw new IllegalArgumentException("estimatedDispatchDate is required when status is ACCEPTED");
        }
        if (newStatus == OrderStatus.REJECTED
                && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException("rejectionReason is required when status is REJECTED");
        }
        this.id = id;
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actorId = actorId;
        this.timestamp = timestamp;
        this.estimatedDispatchDate = estimatedDispatchDate;
        this.rejectionReason = rejectionReason;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public OrderStatus getPreviousStatus() { return previousStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public String getActorId() { return actorId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocalDate getEstimatedDispatchDate() { return estimatedDispatchDate; }
    public String getRejectionReason() { return rejectionReason; }
}
