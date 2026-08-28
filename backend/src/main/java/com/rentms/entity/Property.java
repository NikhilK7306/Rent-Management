package com.rentms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_properties_property_code", columnList = "property_code", unique = true),
        @Index(name = "idx_properties_property_name", columnList = "property_name"),
        @Index(name = "idx_properties_status", columnList = "status"),
        @Index(name = "idx_properties_property_type", columnList = "property_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_name", nullable = false, length = 100)
    private String propertyName;

    @Column(name = "property_code", nullable = false, unique = true, length = 20)
    private String propertyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 1000)
    private String description;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<Rent> rents;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PropertyType {
        HOUSE,
        APARTMENT,
        ROOM,
        SHOP,
        OFFICE,
        OTHER
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}