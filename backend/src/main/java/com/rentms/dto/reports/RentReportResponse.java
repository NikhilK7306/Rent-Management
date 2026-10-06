package com.rentms.dto.reports;

import com.rentms.entity.Rent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentReportResponse {

    private long totalRecords;
    private BigDecimal totalRent;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private long paidCount;
    private long partialCount;
    private long pendingCount;
    private List<RentReportItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentReportItem {
        private Long rentId;
        private String tenantName;
        private String tenantMobile;
        private String propertyName;
        private String propertyCode;
        private int month;
        private int year;
        private String period;
        private BigDecimal rentAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private String status;
        private LocalDate dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyRentOverview {
        private int month;
        private int year;
        private String monthName;
        private BigDecimal rentDue;
        private BigDecimal collected;
        private BigDecimal outstanding;
        private long paidCount;
        private long partialCount;
        private long pendingCount;
        private long overdueCount;
    }
}