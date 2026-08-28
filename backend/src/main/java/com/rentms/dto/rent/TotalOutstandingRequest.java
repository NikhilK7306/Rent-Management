package com.rentms.dto.rent;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalOutstandingRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "As-of date is required")
    private LocalDate asOfDate;

    @NotNull(message = "Total outstanding amount is required")
    @DecimalMin(value = "0.01", message = "Total outstanding amount must be greater than zero")
    private BigDecimal totalOutstandingAmount;

    private Integer numberOfMonths;

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TotalOutstandingResult {
        private int createdCount;
        private BigDecimal monthlyAmount;
        private BigDecimal totalAmount;
        private boolean isConsolidated;
    }
}