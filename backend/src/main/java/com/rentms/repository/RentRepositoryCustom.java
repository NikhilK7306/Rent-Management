package com.rentms.repository;

import com.rentms.dto.reports.RentReportResponse;
import com.rentms.entity.Rent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RentRepositoryCustom {
    Page<Rent> searchRents(String search, Rent.Status status, Integer month, Integer year, String overdue, Pageable pageable);

    BigDecimal sumMonthlyRentByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);

    BigDecimal sumPaidAmountByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);

    long countByStatusAndFilters(Rent.Status status, Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);

    List<RentReportResponse.MonthlyRentOverview> getMonthlyRentOverview(Integer year);

    Page<RentReportResponse.RentReportItem> getRentReport(RentReportRequest request, Pageable pageable);

    List<Object[]> getRentStatusReport(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate);
}