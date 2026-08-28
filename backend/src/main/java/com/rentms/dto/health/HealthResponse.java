package com.rentms.dto.health;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthResponse {

    private String status = "UP";
    private String service = "rent-management-backend";
}