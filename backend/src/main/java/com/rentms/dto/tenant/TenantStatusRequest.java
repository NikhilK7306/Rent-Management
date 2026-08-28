package com.rentms.dto.tenant;

import com.rentms.entity.Tenant;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantStatusRequest {

    @NotNull(message = "Status is required")
    private Tenant.Status status;
}