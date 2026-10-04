package com.rentms.dto.reports;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutstandingReportResponse {

    private BigDecimal totalOutstanding;
    private long totalTenants;
    private long totalRecords;
    private List<OutstandingReportItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutstandingReportItem {
        private Long rentId;
        private String tenantName;
        private String tenantMobile;
        private String propertyName;
        private String propertyCode;
        private int month;
        private int year;
        private String period;
        private BigDecimal rentAmount;
        private BigDecimal totalPaid;
        private BigDecimal outstandingAmount;
        private String status;
    }
}