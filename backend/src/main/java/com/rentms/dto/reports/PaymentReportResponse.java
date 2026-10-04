package com.rentms.dto.reports;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportResponse {

    private long totalRecords;
    private BigDecimal totalAmount;
    private long completedCount;
    private long partialCount;
    private List<PaymentMethodBreakdown> paymentMethodBreakdown;
    private List<PaymentReportItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodBreakdown {
        private String paymentMethod;
        private BigDecimal totalAmount;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentReportItem {
        private Long paymentId;
        private String tenantName;
        private String propertyName;
        private String propertyCode;
        private int rentMonth;
        private int rentYear;
        private String rentPeriod;
        private LocalDate paymentDate;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String referenceNumber;
    }
}