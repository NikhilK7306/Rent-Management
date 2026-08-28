package com.rentms.dto.tenant;

import com.rentms.entity.Tenant;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

    private Long id;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String address;
    private Tenant.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PropertyInfo property;

    public static TenantResponse from(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .fullName(tenant.getFullName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .address(tenant.getAddress())
                .status(tenant.getStatus())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .property(null)
                .build();
    }

    public static TenantResponse from(Tenant tenant, PropertyInfo propertyInfo) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .fullName(tenant.getFullName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .address(tenant.getAddress())
                .status(tenant.getStatus())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .property(propertyInfo)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyInfo {
        private Long id;
        private String propertyCode;
        private String propertyName;
        private String propertyType;
    }
}