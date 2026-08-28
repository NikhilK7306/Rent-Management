package com.rentms.dto.rent;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentStatusRequest {

    @NotNull(message = "Status is required")
    private Status status;

    public enum Status {
        PENDING,
        PAID,
        OVERDUE
    }
}