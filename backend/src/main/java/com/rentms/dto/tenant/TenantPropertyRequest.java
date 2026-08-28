package com.rentms.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPropertyRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;
}