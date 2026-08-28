package com.rentms.dto.rent;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.rentms.entity.Rent;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkRentRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Start month is required")
    @Min(value = 1, message = "Start month must be between 1 and 12")
    @Max(value = 12, message = "Start month must be between 1 and 12")
    private Integer startMonth;

    @NotNull(message = "Start year is required")
    @Min(value = 2020, message = "Start year must be 2020 or later")
    @Max(value = 2100, message = "Start year must be 2100 or earlier")
    private Integer startYear;

    @NotNull(message = "End month is required")
    @Min(value = 1, message = "End month must be between 1 and 12")
    @Max(value = 12, message = "End month must be between 1 and 12")
    private Integer endMonth;

    @NotNull(message = "End year is required")
    @Min(value = 2020, message = "End year must be 2020 or later")
    @Max(value = 2100, message = "End year must be 2100 or earlier")
    private Integer endYear;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than zero")
    private BigDecimal monthlyRent;

    private LocalDate dueDate;

    private Rent.Status initialStatus = Rent.Status.PENDING;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkRentResult {
        private int createdCount;
        private int skippedCount;
        private List<SkippedMonth> skippedMonths;
        private BigDecimal totalAmount;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SkippedMonth {
            private Integer month;
            private Integer year;
            private String reason;
        }
    }
}