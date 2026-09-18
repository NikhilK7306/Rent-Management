package com.rentms.controller;

import com.rentms.dto.reports.*;
import com.rentms.entity.Payment;
import com.rentms.entity.Rent;
import com.rentms.entity.User;
import com.rentms.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/rents")
    public ResponseEntity<RentReportResponse> getRentReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Rent.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RentReportRequest request = RentReportRequest.builder()
                .month(month)
                .year(year)
                .fromDate(fromDate != null ? LocalDate.parse(fromDate) : null)
                .toDate(toDate != null ? LocalDate.parse(toDate) : null)
                .tenantId(tenantId)
                .propertyId(propertyId)
                .status(status)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(reportsService.getRentReport(request));
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentReportResponse> getPaymentReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Payment.Status status,
            @RequestParam(required = false) Payment.PaymentMethod paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaymentReportRequest request = PaymentReportRequest.builder()
                .month(month)
                .year(year)
                .fromDate(fromDate != null ? LocalDate.parse(fromDate) : null)
                .toDate(toDate != null ? LocalDate.parse(toDate) : null)
                .tenantId(tenantId)
                .propertyId(propertyId)
                .status(status)
                .paymentMethod(paymentMethod)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(reportsService.getPaymentReport(request));
    }

    @GetMapping("/outstanding")
    public ResponseEntity<OutstandingReportResponse> getOutstandingReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        OutstandingReportRequest request = OutstandingReportRequest.builder()
                .month(month)
                .year(year)
                .fromDate(fromDate != null ? LocalDate.parse(fromDate) : null)
                .toDate(toDate != null ? LocalDate.parse(toDate) : null)
                .tenantId(tenantId)
                .propertyId(propertyId)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(reportsService.getOutstandingReport(request));
    }

    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<TenantReportResponse> getTenantReport(
            @AuthenticationPrincipal User user,
            @PathVariable Long tenantId
    ) {
        return ResponseEntity.ok(reportsService.getTenantReport(tenantId));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PropertyReportResponse> getPropertyReport(
            @AuthenticationPrincipal User user,
            @PathVariable Long propertyId
    ) {
        return ResponseEntity.ok(reportsService.getPropertyReport(propertyId));
    }

    @GetMapping("/rent-status")
    public ResponseEntity<RentStatusReportResponse> getRentStatusReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        return ResponseEntity.ok(reportsService.getRentStatusReport(
                month, year,
                fromDate != null ? LocalDate.parse(fromDate) : null,
                toDate != null ? LocalDate.parse(toDate) : null
        ));
    }
}