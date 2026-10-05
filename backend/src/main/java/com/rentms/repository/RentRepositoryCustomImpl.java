package com.rentms.repository;

import com.rentms.dto.reports.RentReportRequest;
import com.rentms.dto.reports.RentReportResponse;
import com.rentms.entity.Rent;
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
public class RentRepositoryCustomImpl implements RentRepositoryCustom {

    private final EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Page<Rent> searchRents(String search, Rent.Status status, Integer month, Integer year, String overdue, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (")
                    .append("LOWER(t.full_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(t.mobile_number) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(p.property_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(p.property_code) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(r.status) LIKE LOWER(CONCAT('%', ?, '%'))")
                    .append(")");
            params.add(search);
            params.add(search);
            params.add(search);
            params.add(search);
            params.add(search);
        }

        if (status != null) {
            whereClause.append(" AND r.status = ?");
            params.add(status.name());
        }

        if (month != null) {
            whereClause.append(" AND r.rent_month = ?");
            params.add(month);
        }

        if (year != null) {
            whereClause.append(" AND r.rent_year = ?");
            params.add(year);
        }

        if (overdue != null && !overdue.isEmpty()) {
            if (overdue.equals("true")) {
                whereClause.append(" AND r.status != 'PAID' AND r.due_date < CURRENT_DATE");
            } else if (overdue.equals("false")) {
                whereClause.append(" AND (r.status = 'PAID' OR r.due_date >= CURRENT_DATE)");
            }
        }

        String countSql = "SELECT COUNT(*) FROM rents r " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties p ON r.property_id = p.id " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        StringBuilder dataSql = new StringBuilder("SELECT r.* FROM rents r ")
                .append("JOIN tenants t ON r.tenant_id = t.id ")
                .append("JOIN properties p ON r.property_id = p.id ")
                .append(whereClause)
                .append(" ORDER BY r.rent_year DESC, r.rent_month DESC, r.created_at DESC");

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), Rent.class);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Rent> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public BigDecimal sumMonthlyRentByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "r");

        String sql = "SELECT COALESCE(SUM(r.monthly_rent), 0) FROM rents r " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties p ON r.property_id = p.id " + whereClause;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        Object result = query.getSingleResult();
        return toBigDecimal(result);
    }

    @Override
    public BigDecimal sumPaidAmountByFilters(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "r");

        String sql = "SELECT COALESCE(SUM(r.paid_amount), 0) FROM rents r " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties p ON r.property_id = p.id " + whereClause;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        Object result = query.getSingleResult();
        return toBigDecimal(result);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public long countByStatusAndFilters(Rent.Status status, Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        whereClause.append(" AND r.status = ?");
        params.add(status.name());

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "r");

        String sql = "SELECT COUNT(*) FROM rents r " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties p ON r.property_id = p.id " + whereClause;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public List<RentReportResponse.MonthlyRentOverview> getMonthlyRentOverview(Integer year) {
        String sql = """
            SELECT 
                r.rent_month as month,
                r.rent_year as year,
                COALESCE(SUM(r.monthly_rent), 0) as rent_due,
                COALESCE(SUM(r.paid_amount), 0) as collected,
                COUNT(CASE WHEN r.status = 'PAID' THEN 1 END) as paid_count,
                COUNT(CASE WHEN r.status = 'PARTIAL' THEN 1 END) as partial_count,
                COUNT(CASE WHEN r.status IN ('PENDING', 'OVERDUE') THEN 1 END) as pending_count
            FROM rents r
            WHERE r.rent_year = ?
            GROUP BY r.rent_year, r.rent_month
            ORDER BY r.rent_year, r.rent_month
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, year);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<RentReportResponse.MonthlyRentOverview> overview = new ArrayList<>();
        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue();
            int yr = ((Number) row[1]).intValue();
            BigDecimal rentDue = (BigDecimal) row[2];
            BigDecimal collected = (BigDecimal) row[3];
            long paidCount = ((Number) row[4]).longValue();
            long partialCount = ((Number) row[5]).longValue();
            long pendingCount = ((Number) row[6]).longValue();
            BigDecimal outstanding = rentDue.subtract(collected);

            String monthName = java.time.Month.of(month).name();

            overview.add(RentReportResponse.MonthlyRentOverview.builder()
                    .month(month)
                    .year(yr)
                    .monthName(monthName)
                    .rentDue(rentDue)
                    .collected(collected)
                    .outstanding(outstanding)
                    .paidCount(paidCount)
                    .partialCount(partialCount)
                    .pendingCount(pendingCount)
                    .build());
        }
        return overview;
    }

    @Override
    public Page<RentReportResponse.RentReportItem> getRentReport(RentReportRequest request, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, request.getTenantId(), request.getPropertyId(),
                request.getMonth(), request.getYear(), request.getFromDate(), request.getToDate(), "r");

        if (request.getStatus() != null) {
            whereClause.append(" AND r.status = ?");
            params.add(request.getStatus().name());
        }

        String countSql = "SELECT COUNT(*) FROM rents r " +
                "JOIN tenants t ON r.tenant_id = t.id " +
                "JOIN properties p ON r.property_id = p.id " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        String dataSql = """
            SELECT 
                r.id, t.full_name, t.mobile_number, p.property_name, p.property_code,
                r.rent_month, r.rent_year, r.monthly_rent, r.paid_amount,
                r.status, r.due_date
            FROM rents r
            JOIN tenants t ON r.tenant_id = t.id
            JOIN properties p ON r.property_id = p.id
            """ + whereClause + """
             ORDER BY r.rent_year DESC, r.rent_month DESC, r.created_at DESC
            """;

        Query dataQuery = entityManager.createNativeQuery(dataSql);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        List<RentReportResponse.RentReportItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal rentAmount = (BigDecimal) row[7];
            BigDecimal paidAmount = row[8] != null ? (BigDecimal) row[8] : BigDecimal.ZERO;
            BigDecimal outstanding = rentAmount.subtract(paidAmount);

            int month = ((Number) row[5]).intValue();
            int year = ((Number) row[6]).intValue();
            String period = java.time.Month.of(month).name() + " " + year;

            items.add(RentReportResponse.RentReportItem.builder()
                    .rentId(((Number) row[0]).longValue())
                    .tenantName((String) row[1])
                    .tenantMobile((String) row[2])
                    .propertyName((String) row[3])
                    .propertyCode((String) row[4])
                    .month(month)
                    .year(year)
                    .period(period)
                    .rentAmount(rentAmount)
                    .paidAmount(paidAmount)
                    .outstandingAmount(outstanding)
                    .status((String) row[9])
                    .dueDate(((java.sql.Date) row[10]).toLocalDate())
                    .build());
        }

        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public List<Object[]> getRentStatusReport(Long tenantId, Long propertyId, Integer month, Integer year, LocalDate fromDate, LocalDate toDate) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        addFilters(whereClause, params, tenantId, propertyId, month, year, fromDate, toDate, "r");

        String sql = """
            SELECT 
                r.status,
                COUNT(*) as count,
                COALESCE(SUM(r.monthly_rent), 0) as total_rent,
                COALESCE(SUM(r.paid_amount), 0) as total_paid
            FROM rents r
            JOIN tenants t ON r.tenant_id = t.id
            JOIN properties p ON r.property_id = p.id
            """ + whereClause + """
             GROUP BY r.status
            """;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        return query.getResultList();
    }

    private void addFilters(StringBuilder whereClause, List<Object> params, Long tenantId, Long propertyId,
                            Integer month, Integer year, LocalDate fromDate, LocalDate toDate, String alias) {
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
            whereClause.append(" AND r.due_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            whereClause.append(" AND r.due_date <= ?");
            params.add(toDate);
        }
    }
}