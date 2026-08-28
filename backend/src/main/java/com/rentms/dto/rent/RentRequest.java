package com.rentms.dto.rent;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Rent month is required")
    @Min(value = 1, message = "Rent month must be between 1 and 12")
    @Max(value = 12, message = "Rent month must be between 1 and 12")
    private Integer rentMonth;

    @NotNull(message = "Rent year is required")
    @Min(value = 2020, message = "Rent year must be 2020 or later")
    @Max(value = 2100, message = "Rent year must be 2100 or earlier")
    private Integer rentYear;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than zero")
    private BigDecimal monthlyRent;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}