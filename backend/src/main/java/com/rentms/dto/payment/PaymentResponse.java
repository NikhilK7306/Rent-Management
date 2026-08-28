package com.rentms.dto.payment;

import com.rentms.entity.Payment;
import com.rentms.entity.Rent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private RentInfo rent;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private Status status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(Payment payment) {
        RentInfo rentInfo = null;
        if (payment.getRent() != null) {
            Rent rent = payment.getRent();
            rentInfo = RentInfo.builder()
                    .id(rent.getId())
                    .tenant(TenantInfo.builder()
                            .id(rent.getTenant().getId())
                            .fullName(rent.getTenant().getFullName())
                            .mobileNumber(rent.getTenant().getMobileNumber())
                            .build())
                    .property(PropertyInfo.builder()
                            .id(rent.getProperty().getId())
                            .propertyCode(rent.getProperty().getPropertyCode())
                            .propertyName(rent.getProperty().getPropertyName())
                            .propertyType(rent.getProperty().getPropertyType().name())
                            .monthlyRent(rent.getProperty().getMonthlyRent())
                            .build())
                    .rentMonth(rent.getRentMonth())
                    .rentYear(rent.getRentYear())
                    .monthlyRent(rent.getMonthlyRent())
                    .dueDate(rent.getDueDate())
                    .status(rent.getStatus())
                    .paidDate(rent.getPaidDate())
                    .paidAmount(rent.getPaidAmount())
                    .build();
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .rent(rentInfo)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(PaymentMethod.valueOf(payment.getPaymentMethod().name()))
                .referenceNumber(payment.getReferenceNumber())
                .status(convertToResponseStatus(payment.getStatus()))
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private static Status convertToResponseStatus(Payment.Status entityStatus) {
        return switch (entityStatus) {
            case PENDING -> Status.PENDING;
            case PARTIAL -> Status.PARTIAL;
            case PAID -> Status.PAID;
            case OVERDUE -> Status.OVERDUE;
            case CANCELLED -> Status.CANCELLED;
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentInfo {
        private Long id;
        private TenantInfo tenant;
        private PropertyInfo property;
        private Integer rentMonth;
        private Integer rentYear;
        private BigDecimal monthlyRent;
        private LocalDate dueDate;
        private Rent.Status status;
        private LocalDate paidDate;
        private BigDecimal paidAmount;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyInfo {
        private Long id;
        private String propertyCode;
        private String propertyName;
        private String propertyType;
        private BigDecimal monthlyRent;
    }

    public enum PaymentMethod {
        CASH,
        UPI,
        CARD,
        BANK_TRANSFER,
        CHEQUE,
        OTHER
    }

    public enum Status {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE,
        CANCELLED
    }
}