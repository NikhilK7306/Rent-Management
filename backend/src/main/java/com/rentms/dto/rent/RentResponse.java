package com.rentms.dto.rent;

import com.rentms.entity.Property;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentResponse {

    private Long id;
    private TenantInfo tenant;
    private PropertyInfo property;
    private Integer rentMonth;
    private Integer rentYear;
    private BigDecimal monthlyRent;
    private LocalDate dueDate;
    private Status status;
    private LocalDate paidDate;
    private BigDecimal paidAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RentResponse from(Rent rent) {
        TenantInfo tenantInfo = null;
        PropertyInfo propertyInfo = null;

        if (rent.getTenant() != null) {
            tenantInfo = TenantInfo.builder()
                    .id(rent.getTenant().getId())
                    .fullName(rent.getTenant().getFullName())
                    .mobileNumber(rent.getTenant().getMobileNumber())
                    .build();
        }

        if (rent.getProperty() != null) {
            propertyInfo = PropertyInfo.builder()
                    .id(rent.getProperty().getId())
                    .propertyCode(rent.getProperty().getPropertyCode())
                    .propertyName(rent.getProperty().getPropertyName())
                    .propertyType(rent.getProperty().getPropertyType().name())
                    .monthlyRent(rent.getProperty().getMonthlyRent())
                    .build();
        }

        return RentResponse.builder()
                .id(rent.getId())
                .tenant(tenantInfo)
                .property(propertyInfo)
                .rentMonth(rent.getRentMonth())
                .rentYear(rent.getRentYear())
                .monthlyRent(rent.getMonthlyRent())
                .dueDate(rent.getDueDate())
                .status(convertToResponseStatus(rent.getStatus()))
                .paidDate(rent.getPaidDate())
                .paidAmount(rent.getPaidAmount())
                .createdAt(rent.getCreatedAt())
                .updatedAt(rent.getUpdatedAt())
                .build();
    }

    private static Status convertToResponseStatus(Rent.Status entityStatus) {
        return switch (entityStatus) {
            case PENDING -> Status.PENDING;
            case PARTIAL -> Status.PARTIAL;
            case PAID -> Status.PAID;
            case OVERDUE -> Status.OVERDUE;
        };
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

    public enum Status {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE
    }
}