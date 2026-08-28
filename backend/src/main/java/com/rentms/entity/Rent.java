package com.rentms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rents", indexes = {
        @Index(name = "idx_rents_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_rents_property_id", columnList = "property_id"),
        @Index(name = "idx_rents_status", columnList = "status"),
        @Index(name = "idx_rents_rent_period", columnList = "rent_year, rent_month"),
        @Index(name = "idx_rents_due_date", columnList = "due_date"),
        @Index(name = "idx_rents_tenant_property_period", columnList = "tenant_id, property_id, rent_year, rent_month", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "rent_month", nullable = false)
    private Integer rentMonth;

    @Column(name = "rent_year", nullable = false)
    private Integer rentYear;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE
    }
}