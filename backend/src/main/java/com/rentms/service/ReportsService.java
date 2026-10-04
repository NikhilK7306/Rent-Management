package com.rentms.service;

import com.rentms.dto.reports.*;
import com.rentms.entity.Payment;
import com.rentms.entity.Property;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import com.rentms.exception.TenantNotFoundException;
import com.rentms.exception.PropertyNotFoundException;
import com.rentms.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final RentRepository rentRepository;
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional(readOnly = true)
    public RentReportResponse getRentReport(RentReportRequest request) {
        log.debug("Generating rent report with filters: {}", request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Get total records and aggregates
        BigDecimal totalRent = rentRepository.sumMonthlyRentByFilters(
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate()
        );
        BigDecimal totalPaid = rentRepository.sumPaidAmountByFilters(
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate()
        );
        BigDecimal totalOutstanding = totalRent.subtract(totalPaid);

        long paidCount = rentRepository.countByStatusAndFilters(Rent.Status.PAID,
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate());
        long partialCount = rentRepository.countByStatusAndFilters(Rent.Status.PARTIAL,
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate());
        long pendingCount = rentRepository.countByStatusAndFilters(Rent.Status.PENDING,
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate()) +
                rentRepository.countByStatusAndFilters(Rent.Status.OVERDUE,
                        request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                        request.getFromDate(), request.getToDate());

        // Get paginated items
        Page<RentReportResponse.RentReportItem> itemsPage = rentRepository.getRentReport(request, pageable);

        return RentReportResponse.builder()
                .totalRecords(itemsPage.getTotalElements())
                .totalRent(totalRent)
                .totalPaid(totalPaid)
                .totalOutstanding(totalOutstanding)
                .paidCount(paidCount)
                .partialCount(partialCount)
                .pendingCount(pendingCount)
                .items(itemsPage.getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentReportResponse getPaymentReport(PaymentReportRequest request) {
        log.debug("Generating payment report with filters: {}", request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        BigDecimal totalAmount = paymentRepository.sumAmountByFilters(
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate()
        );

        List<PaymentReportResponse.PaymentMethodBreakdown> paymentMethodBreakdown = paymentRepository.getPaymentMethodBreakdown(
                request.getTenantId(), request.getPropertyId(), request.getMonth(), request.getYear(),
                request.getFromDate(), request.getToDate()
        );

        long completedCount = paymentRepository.countByStatus(Payment.Status.PAID);
        long partialCount = paymentRepository.countByStatus(Payment.Status.PARTIAL);

        Page<PaymentReportResponse.PaymentReportItem> itemsPage = paymentRepository.getPaymentReport(request, pageable);

        return PaymentReportResponse.builder()
                .totalRecords(itemsPage.getTotalElements())
                .totalAmount(totalAmount)
                .completedCount(completedCount)
                .partialCount(partialCount)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .items(itemsPage.getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public OutstandingReportResponse getOutstandingReport(OutstandingReportRequest request) {
        log.debug("Generating outstanding report with filters: {}", request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Get all rents with outstanding amounts
        List<Object[]> results = getOutstandingRentsQuery(request);

        List<OutstandingReportResponse.OutstandingReportItem> items = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (Object[] row : results) {
            BigDecimal rentAmount = (BigDecimal) row[6];
            BigDecimal paidAmount = row[7] != null ? (BigDecimal) row[7] : BigDecimal.ZERO;
            BigDecimal outstanding = rentAmount.subtract(paidAmount);

            if (outstanding.compareTo(BigDecimal.ZERO) > 0) {
                int month = ((Number) row[4]).intValue();
                int year = ((Number) row[5]).intValue();
                String period = java.time.Month.of(month).name() + " " + year;

                items.add(OutstandingReportResponse.OutstandingReportItem.builder()
                        .rentId(((Number) row[0]).longValue())
                        .tenantName((String) row[1])
                        .tenantMobile((String) row[2])
                        .propertyName((String) row[3])
                        .propertyCode((String) row[8])
                        .month(month)
                        .year(year)
                        .period(period)
                        .rentAmount(rentAmount)
                        .totalPaid(paidAmount)
                        .outstandingAmount(outstanding)
                        .status((String) row[9])
                        .build());

                totalOutstanding = totalOutstanding.add(outstanding);
            }
        }

        // Apply pagination manually since we filter by outstanding > 0
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<OutstandingReportResponse.OutstandingReportItem> paginatedItems = items.subList(start, end);

        return OutstandingReportResponse.builder()
                .totalOutstanding(totalOutstanding)
                .totalTenants((long) items.stream().map(OutstandingReportResponse.OutstandingReportItem::getTenantName).distinct().count())
                .totalRecords((long) items.size())
                .items(paginatedItems)
                .build();
    }

    private List<Object[]> getOutstandingRentsQuery(OutstandingReportRequest request) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (request.getTenantId() != null) {
            whereClause.append(" AND r.tenant_id = ?");
            params.add(request.getTenantId());
        }
        if (request.getPropertyId() != null) {
            whereClause.append(" AND r.property_id = ?");
            params.add(request.getPropertyId());
        }
        if (request.getMonth() != null) {
            whereClause.append(" AND r.rent_month = ?");
            params.add(request.getMonth());
        }
        if (request.getYear() != null) {
            whereClause.append(" AND r.rent_year = ?");
            params.add(request.getYear());
        }
        if (request.getFromDate() != null) {
            whereClause.append(" AND r.due_date >= ?");
            params.add(request.getFromDate());
        }
        if (request.getToDate() != null) {
            whereClause.append(" AND r.due_date <= ?");
            params.add(request.getToDate());
        }

        String sql = """
            SELECT 
                r.id, t.full_name, t.mobile_number, p.property_name, r.rent_month, r.rent_year,
                r.monthly_rent, r.paid_amount, p.property_code, r.status
            FROM rents r
            JOIN tenants t ON r.tenant_id = t.id
            JOIN properties p ON r.property_id = p.id
            """ + whereClause + """
             ORDER BY r.rent_year DESC, r.rent_month DESC, r.created_at DESC
            """;

        var query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }

    @Transactional(readOnly = true)
    public TenantReportResponse getTenantReport(Long tenantId) {
        log.debug("Generating tenant report for tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));

        Property property = tenant.getPropertyId() != null
                ? propertyRepository.findById(tenant.getPropertyId()).orElse(null)
                : null;

        // Get all rents for this tenant
        List<Rent> rents = rentRepository.findByTenantId(tenantId);

        BigDecimal totalRentGenerated = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        long paidRents = 0;
        long pendingRents = 0;
        long partialRents = 0;

        List<TenantReportResponse.TenantRentHistoryItem> rentHistory = new ArrayList<>();

        for (Rent rent : rents) {
            totalRentGenerated = totalRentGenerated.add(rent.getMonthlyRent());
            BigDecimal paidAmount = rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO;
            totalPaid = totalPaid.add(paidAmount);

            switch (rent.getStatus()) {
                case PAID -> paidRents++;
                case PARTIAL -> partialRents++;
                case PENDING, OVERDUE -> pendingRents++;
            }

            // Get payments for this rent
            List<Payment> payments = paymentRepository.findByRentId(rent.getId());
            List<TenantReportResponse.PaymentSummaryItem> paymentItems = payments.stream()
                    .map(p -> TenantReportResponse.PaymentSummaryItem.builder()
                            .paymentId(p.getId())
                            .paymentDate(p.getPaymentDate())
                            .amount(p.getAmount())
                            .paymentMethod(p.getPaymentMethod().name())
                            .status(p.getStatus().name())
                            .build())
                    .toList();

            int month = rent.getRentMonth();
            int year = rent.getRentYear();
            String period = java.time.Month.of(month).name() + " " + year;

            rentHistory.add(TenantReportResponse.TenantRentHistoryItem.builder()
                    .rentId(rent.getId())
                    .month(month)
                    .year(year)
                    .period(period)
                    .rentAmount(rent.getMonthlyRent())
                    .paidAmount(paidAmount)
                    .outstandingAmount(rent.getMonthlyRent().subtract(paidAmount))
                    .status(rent.getStatus().name())
                    .dueDate(rent.getDueDate())
                    .payments(paymentItems)
                    .build());
        }

        BigDecimal totalOutstanding = totalRentGenerated.subtract(totalPaid);

        return TenantReportResponse.builder()
                .tenantName(tenant.getFullName())
                .tenantMobile(tenant.getMobileNumber())
                .tenantEmail(tenant.getEmail())
                .propertyName(property != null ? property.getPropertyName() : null)
                .propertyCode(property != null ? property.getPropertyCode() : null)
                .totalRentGenerated(totalRentGenerated)
                .totalPaid(totalPaid)
                .totalOutstanding(totalOutstanding)
                .paidRents(paidRents)
                .pendingRents(pendingRents)
                .partialRents(partialRents)
                .rentHistory(rentHistory)
                .build();
    }

    @Transactional(readOnly = true)
    public PropertyReportResponse getPropertyReport(Long propertyId) {
        log.debug("Generating property report for property: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + propertyId));

        // Get current tenant
        Tenant currentTenant = tenantRepository.findByPropertyIdAndStatus(propertyId, Tenant.Status.ACTIVE).orElse(null);

        // Get all rents for this property
        List<Rent> rents = rentRepository.findByPropertyId(propertyId);

        BigDecimal totalRentGenerated = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;

        List<PropertyReportResponse.PropertyRentHistoryItem> rentHistory = new ArrayList<>();

        for (Rent rent : rents) {
            totalRentGenerated = totalRentGenerated.add(rent.getMonthlyRent());
            BigDecimal paidAmount = rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO;
            totalCollected = totalCollected.add(paidAmount);

            Tenant tenant = rent.getTenant();
            int month = rent.getRentMonth();
            int year = rent.getRentYear();
            String period = java.time.Month.of(month).name() + " " + year;

            rentHistory.add(PropertyReportResponse.PropertyRentHistoryItem.builder()
                    .rentId(rent.getId())
                    .tenantName(tenant.getFullName())
                    .tenantMobile(tenant.getMobileNumber())
                    .month(month)
                    .year(year)
                    .period(period)
                    .rentAmount(rent.getMonthlyRent())
                    .paidAmount(paidAmount)
                    .outstandingAmount(rent.getMonthlyRent().subtract(paidAmount))
                    .status(rent.getStatus().name())
                    .dueDate(rent.getDueDate())
                    .build());
        }

        BigDecimal totalOutstanding = totalRentGenerated.subtract(totalCollected);

        return PropertyReportResponse.builder()
                .propertyName(property.getPropertyName())
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType().name())
                .address(property.getAddress())
                .monthlyRent(property.getMonthlyRent())
                .currentTenantName(currentTenant != null ? currentTenant.getFullName() : null)
                .currentTenantMobile(currentTenant != null ? currentTenant.getMobileNumber() : null)
                .isOccupied(currentTenant != null)
                .totalRentGenerated(totalRentGenerated)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .rentHistory(rentHistory)
                .build();
    }

    @Transactional(readOnly = true)
    public RentStatusReportResponse getRentStatusReport(Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        log.debug("Generating rent status report for month: {}, year: {}", month, year);

        List<Object[]> results = rentRepository.getRentStatusReport(null, null, month, year, fromDate, toDate);

        long paidCount = 0;
        long partialCount = 0;
        long pendingCount = 0;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal partialAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        for (Object[] row : results) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal totalRent = (BigDecimal) row[2];
            BigDecimal totalPaid = (BigDecimal) row[3];

            switch (status) {
                case "PAID" -> {
                    paidCount = count;
                    paidAmount = totalPaid;
                }
                case "PARTIAL" -> {
                    partialCount = count;
                    partialAmount = totalPaid;
                }
                case "PENDING", "OVERDUE" -> {
                    pendingCount += count;
                    pendingAmount = pendingAmount.add(totalRent.subtract(totalPaid));
                }
            }
        }

        return RentStatusReportResponse.builder()
                .paidCount(paidCount)
                .partialCount(partialCount)
                .pendingCount(pendingCount)
                .paidAmount(paidAmount)
                .partialAmount(partialAmount)
                .pendingAmount(pendingAmount)
                .build();
    }
}