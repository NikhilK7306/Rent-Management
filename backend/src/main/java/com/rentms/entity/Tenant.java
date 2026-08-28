package com.rentms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenants_mobile_number", columnList = "mobile_number", unique = true),
        @Index(name = "idx_tenants_full_name", columnList = "full_name"),
        @Index(name = "idx_tenants_status", columnList = "status"),
        @Index(name = "idx_tenants_property_id", columnList = "property_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
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

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}