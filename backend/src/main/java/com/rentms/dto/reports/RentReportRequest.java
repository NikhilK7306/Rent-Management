package com.rentms.dto.reports;

import com.rentms.entity.Rent;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private Rent.Status status;
    private int page = 0;
    private int size = 20;
}