package org.ups.dropshippingservicefinal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DropshippingOrder {

    private final Long id;
    private final String orderCode;
    private final Long providerId;
    private final String productCode;
    private final String productDescription;
    private final Integer quantity;
    private final Address deliveryAddress;
    private final String customerName;
    private final String customerContact;
    private final LocalDate expectedDeliveryDate;
    private final String specialConditions;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DropshippingOrder(Long id, String orderCode, Long providerId, String productCode,
                             String productDescription, Integer quantity, Address deliveryAddress,
                             String customerName, String customerContact,
                             LocalDate expectedDeliveryDate, String specialConditions,
                             OrderStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.id = id;
        this.orderCode = orderCode;
        this.providerId = providerId;
        this.productCode = productCode;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.deliveryAddress = deliveryAddress;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.specialConditions = specialConditions;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this.status != OrderStatus.PENDING) {
            return false;
        }
        return newStatus == OrderStatus.ACCEPTED || newStatus == OrderStatus.REJECTED;
    }

    public Long getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public Long getProviderId() { return providerId; }
    public String getProductCode() { return productCode; }
    public String getProductDescription() { return productDescription; }
    public Integer getQuantity() { return quantity; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public String getCustomerName() { return customerName; }
    public String getCustomerContact() { return customerContact; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public String getSpecialConditions() { return specialConditions; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(OrderStatus status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
