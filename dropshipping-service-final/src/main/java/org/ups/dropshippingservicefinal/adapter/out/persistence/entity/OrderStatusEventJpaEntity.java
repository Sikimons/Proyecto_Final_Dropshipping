package org.ups.dropshippingservicefinal.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private DropshippingOrderJpaEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private OrderStatus newStatus;

    @Column(name = "actor_id", nullable = false, length = 100)
    private String actorId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "estimated_dispatch_date")
    private LocalDate estimatedDispatchDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
