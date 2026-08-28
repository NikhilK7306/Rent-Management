package com.rentms.dto.property;

import com.rentms.entity.Property;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRequest {

    @NotBlank(message = "Property name is required")
    @Size(max = 100, message = "Property name must not exceed 100 characters")
    private String propertyName;

    @NotBlank(message = "Property code is required")
    @Size(max = 20, message = "Property code must not exceed 20 characters")
    private String propertyCode;

    @NotNull(message = "Property type is required")
    private Property.PropertyType propertyType;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than zero")
    private BigDecimal monthlyRent;
}