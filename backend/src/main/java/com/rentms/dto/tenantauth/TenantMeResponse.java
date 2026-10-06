package com.rentms.dto.tenantauth;

import com.rentms.entity.Tenant;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMeResponse {

    private Long id;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String address;
    private Tenant.Status status;
    private PropertyInfo property;

    public static TenantMeResponse from(Tenant tenant) {
        return TenantMeResponse.builder()
                .id(tenant.getId())
                .fullName(tenant.getFullName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .address(tenant.getAddress())
                .status(tenant.getStatus())
                .property(tenant.getPropertyId() != null ? PropertyInfo.from(tenant) : null)
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
        private String address;
        private String monthlyRent;

        public static PropertyInfo from(Tenant tenant) {
            return PropertyInfo.builder()
                    .id(tenant.getPropertyId())
                    .build();
        }
    }
}