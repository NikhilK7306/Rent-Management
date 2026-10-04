package com.rentms.dto.reports;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyReportResponse {

    private String propertyName;
    private String propertyCode;
    private String propertyType;
    private String address;
    private BigDecimal monthlyRent;
    private String currentTenantName;
    private String currentTenantMobile;
    private boolean isOccupied;
    private BigDecimal totalRentGenerated;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private List<PropertyRentHistoryItem> rentHistory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyRentHistoryItem {
        private Long rentId;
        private String tenantName;
        private String tenantMobile;
        private int month;
        private int year;
        private String period;
        private BigDecimal rentAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private String status;
        private LocalDate dueDate;
    }
}