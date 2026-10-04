package com.rentms.dto.reports;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentStatusReportResponse {
    private long paidCount;
    private long partialCount;
    private long pendingCount;
    private BigDecimal paidAmount;
    private BigDecimal partialAmount;
    private BigDecimal pendingAmount;
}