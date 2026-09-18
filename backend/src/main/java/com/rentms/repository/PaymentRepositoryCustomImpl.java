package com.rentms.repository;

import com.rentms.dto.reports.PaymentReportRequest;
import com.rentms.dto.reports.PaymentReportResponse;
import com.rentms.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<Payment> searchPayments(
            String search,
            Long rentId,
            Payment.Status status,
            Payment.PaymentMethod paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (")
                    .append("LOWER(t.full_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(t.mobile_number) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(pr.property_code) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(pr.property_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR CAST(p.reference_number AS TEXT) LIKE LOWER(CONCAT('%', ?, '%'))")
                    .append(")");
            for (int i = 0; i < 5; i++) {
                params.add(search);
            }
        }

        if (rentId != null) {
            whereClause.append(" AND p.rent_id = ?");
            params.add(rentId);
        }

        if (status != null) {
            whereClause.append(" AND p.status = ?");
            params.add(status.name());
        }

        if (paymentMethod != null) {
            whereClause.append(" AND p.payment_method = ?");
            params.add(paymentMethod.name());
        }

        if (startDate != null) {
            whereClause.append(" AND p.payment_date >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            whereClause.append(" AND p.payment_date <= ?");
            params.add(endDate);
        }

        String countSql = "SELECT COUNT(*) FROM payments p " +
                "JOIN rents r ON p.rent_id = r.id " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties pr ON r.property_id = pr.id " +
                whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        StringBuilder dataSql = new StringBuilder("SELECT p.* FROM payments p ")
                .append("JOIN rents r ON p.rent_id = r.id ")
                .append("JOIN tenants t ON r.tenant_id = t.id ")
                .append("JOIN properties pr ON r.property_id = pr.id ")
                .append(whereClause)
                .append(" ORDER BY p.payment_date DESC, p.created_at DESC");

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), Payment.class);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Payment> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public BigDecimal sumAmountByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "p", "r");

        String sql = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p " +
                "JOIN rents r ON p.rent_id = r.id " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties pr ON r.property_id = pr.id " + whereClause;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        return (BigDecimal) query.getSingleResult();
    }

    @Override
    public List<PaymentReportResponse.PaymentMethodBreakdown> getPaymentMethodBreakdown(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "p", "r");

        String sql = """
            SELECT 
                p.payment_method,
                COALESCE(SUM(p.amount), 0) as total_amount,
                COUNT(*) as count
            FROM payments p
            JOIN rents r ON p.rent_id = r.id
            JOIN tenants t ON r.tenant_id = t.id
            JOIN properties pr ON r.property_id = pr.id
            """ + whereClause + """
            GROUP BY p.payment_method
            ORDER BY total_amount DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<PaymentReportResponse.PaymentMethodBreakdown> breakdown = new ArrayList<>();
        for (Object[] row : rows) {
            breakdown.add(PaymentReportResponse.PaymentMethodBreakdown.builder()
                    .paymentMethod((String) row[0])
                    .totalAmount((BigDecimal) row[1])
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return breakdown;
    }

    @Override
    public Page<PaymentReportResponse.PaymentReportItem> getPaymentReport(PaymentReportRequest request, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, request.getTenantId(), request.getPropertyId(),
                request.getMonth(), request.getYear(), request.getFromDate(), request.getToDate(), "p", "r");

        if (request.getStatus() != null) {
            whereClause.append(" AND p.status = ?");
            params.add(request.getStatus().name());
        }
        if (request.getPaymentMethod() != null) {
            whereClause.append(" AND p.payment_method = ?");
            params.add(request.getPaymentMethod().name());
        }

        String countSql = "SELECT COUNT(*) FROM payments p " +
                "JOIN rents r ON p.rent_id = r.id " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties pr ON r.property_id = pr.id " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        String dataSql = """
            SELECT 
                p.id, t.full_name, pr.property_name, pr.property_code,
                r.rent_month, r.rent_year, p.payment_date, p.amount,
                p.payment_method, p.status, p.reference_number
            FROM payments p
            JOIN rents r ON p.rent_id = r.id
            JOIN tenants t ON r.tenant_id = t.id
            JOIN properties pr ON r.property_id = pr.id
            """ + whereClause + """
            ORDER BY p.payment_date DESC, p.created_at DESC
            """;

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        List<PaymentReportResponse.PaymentReportItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            int month = ((Number) row[4]).intValue();
            int year = ((Number) row[5]).intValue();
            String period = java.time.Month.of(month).name() + " " + year;

            items.add(PaymentReportResponse.PaymentReportItem.builder()
                    .paymentId(((Number) row[0]).longValue())
                    .tenantName((String) row[1])
                    .propertyName((String) row[2])
                    .propertyCode((String) row[3])
                    .rentMonth(month)
                    .rentYear(year)
                    .rentPeriod(period)
                    .paymentDate((LocalDate) row[6])
                    .amount((BigDecimal) row[7])
                    .paymentMethod((String) row[8])
                    .status((String) row[9])
                    .referenceNumber((String) row[10])
                    .build());
        }

        return new PageImpl<>(items, pageable, total);
    }

    private void addFilters(StringBuilder whereClause, List<Object> params, Long tenantId, Long propertyId,
                            Integer month, Integer year, LocalDate fromDate, LocalDate toDate, String paymentAlias, String rentAlias) {
        if (tenantId != null) {
            whereClause.append(" AND r.tenant_id = ?");
            params.add(tenantId);
        }
        if (propertyId != null) {
            whereClause.append(" AND r.property_id = ?");
            params.add(propertyId);
        }
        if (month != null) {
            whereClause.append(" AND r.rent_month = ?");
            params.add(month);
        }
        if (year != null) {
            whereClause.append(" AND r.rent_year = ?");
            params.add(year);
        }
        if (fromDate != null) {
            whereClause.append(" AND p.payment_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            whereClause.append(" AND p.payment_date <= ?");
            params.add(toDate);
        }
    }
}