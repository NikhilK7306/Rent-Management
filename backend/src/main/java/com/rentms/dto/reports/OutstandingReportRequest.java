package com.rentms.dto.reports;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutstandingReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private int page = 0;
    private int size = 20;
}