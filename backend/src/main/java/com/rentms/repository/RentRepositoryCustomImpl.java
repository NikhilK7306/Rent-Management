package com.rentms.repository;

import com.rentms.entity.Rent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RentRepositoryCustomImpl implements RentRepositoryCustom {

    private final EntityManager entityManager;

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
}