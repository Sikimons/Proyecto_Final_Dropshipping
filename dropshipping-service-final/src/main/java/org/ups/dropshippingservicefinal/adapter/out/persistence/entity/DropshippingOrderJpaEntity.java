package org.ups.dropshippingservicefinal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dropshipping_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropshippingOrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true, nullable = false, length = 50)
    private String orderCode;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "product_code", nullable = false, length = 100)
    private String productCode;

    @Column(name = "product_description", nullable = false, length = 500)
    private String productDescription;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_contact", nullable = false, length = 200)
    private String customerContact;

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
