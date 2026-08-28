package com.rentms.dto.property;

import com.rentms.entity.Property;
import com.rentms.entity.Tenant;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {

    private Long id;
    private String propertyName;
    private String propertyCode;
    private Property.PropertyType propertyType;
    private String address;
    private String description;
    private BigDecimal monthlyRent;
    private Property.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tenantName;
    private TenantInfo tenant;

    public static PropertyResponse from(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType())
                .address(property.getAddress())
                .description(property.getDescription())
                .monthlyRent(property.getMonthlyRent())
                .status(property.getStatus())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .tenantName("Not Assigned")
                .tenant(null)
                .build();
    }

    public static PropertyResponse from(Property property, Tenant tenant) {
        TenantInfo tenantInfo = null;
        String tenantName = "Not Assigned";
        if (tenant != null) {
            tenantName = tenant.getFullName();
            tenantInfo = TenantInfo.builder()
                    .id(tenant.getId())
                    .fullName(tenant.getFullName())
                    .mobileNumber(tenant.getMobileNumber())
                    .build();
        }
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType())
                .address(property.getAddress())
                .description(property.getDescription())
                .monthlyRent(property.getMonthlyRent())
                .status(property.getStatus())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .tenantName(tenantName)
                .tenant(tenantInfo)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TenantInfo {
        private Long id;
        private String fullName;
        private String mobileNumber;
    }
}