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
public class RentReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private Rent.Status status;
    private int page = 0;
    private int size = 20;
}

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
}

package com.rentms.dto.reports;

import com.rentms.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private Payment.Status status;
    private Payment.PaymentMethod paymentMethod;
    private int page = 0;
    private int size = 20;
}

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutstandingReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private int page = 0;
    private int size = 20;
}

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentStatusReportResponse {
    private long paidCount;
    private long partialCount;
    private long pendingCount;
    private BigDecimal paidAmount;
    private BigDecimal partialAmount;
    private BigDecimal pendingAmount;
}