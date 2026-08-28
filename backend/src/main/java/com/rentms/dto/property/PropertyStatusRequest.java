package com.rentms.dto.property;

import com.rentms.entity.Property;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyStatusRequest {

    @NotNull(message = "Status is required")
    private Property.Status status;
}