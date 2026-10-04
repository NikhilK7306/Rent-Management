package com.rentms.repository;

import com.rentms.dto.reports.PaymentReportRequest;
import com.rentms.dto.reports.PaymentReportResponse;
import com.rentms.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepositoryCustom {

    Page<Payment> searchPayments(
            String search,
            Long rentId,
            Payment.Status status,
            Payment.PaymentMethod paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    BigDecimal sumAmountByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);

    List<PaymentReportResponse.PaymentMethodBreakdown> getPaymentMethodBreakdown(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);

    Page<PaymentReportResponse.PaymentReportItem> getPaymentReport(PaymentReportRequest request, Pageable pageable);
}