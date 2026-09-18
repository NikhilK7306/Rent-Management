package com.rentms.dto.dashboard;

import com.rentms.dto.notification.NotificationSummaryResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private String message;
    private String adminName;
    private String role;

    private long totalProperties;
    private long occupiedProperties;
    private long vacantProperties;
    private long activeTenants;

    private BigDecimal totalRentDue;
    private BigDecimal totalRentCollected;
    private BigDecimal totalOutstanding;
    private long pendingRents;

    private RentSummary rentSummary;
    private PaymentSummary paymentSummary;
    private List<MonthlyRentOverview> monthlyRentOverview;
    private NotificationSummaryResponse notificationSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentSummary {
        private BigDecimal totalRent;
        private BigDecimal paidRent;
        private BigDecimal partiallyPaidRent;
        private BigDecimal pendingRent;
        private BigDecimal outstandingAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummary {
        private long totalPayments;
        private BigDecimal totalAmountCollected;
        private long completedPayments;
        private long partialPayments;
        private List<PaymentMethodBreakdown> paymentMethodBreakdown;
    }

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
    }
}