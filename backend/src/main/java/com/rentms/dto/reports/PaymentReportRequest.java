package com.rentms.dto.reports;

import com.rentms.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportRequest {
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long tenantId;
    private Long propertyId;
    private Payment.Status status;
    private Payment.PaymentMethod paymentMethod;
    private int page = 0;
    private int size = 20;
}