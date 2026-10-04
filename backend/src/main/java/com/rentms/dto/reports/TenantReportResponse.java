package com.rentms.dto.reports;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantReportResponse {

    private String tenantName;
    private String tenantMobile;
    private String tenantEmail;
    private String propertyName;
    private String propertyCode;
    private BigDecimal totalRentGenerated;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private long paidRents;
    private long pendingRents;
    private long partialRents;
    private List<TenantRentHistoryItem> rentHistory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TenantRentHistoryItem {
        private Long rentId;
        private int month;
        private int year;
        private String period;
        private BigDecimal rentAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private String status;
        private LocalDate dueDate;
        private List<PaymentSummaryItem> payments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummaryItem {
        private Long paymentId;
        private LocalDate paymentDate;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
    }
}