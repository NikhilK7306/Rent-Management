package com.rentms.dto.tenantauth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDashboardResponse {

    private String tenantName;
    private String mobileNumber;
    private String email;
    private PropertyInfo property;
    private RentSummary rentSummary;

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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentSummary {
        private String currentMonthRent;
        private String paidAmount;
        private String outstandingAmount;
        private String status;
        private Integer rentMonth;
        private Integer rentYear;
    }
}