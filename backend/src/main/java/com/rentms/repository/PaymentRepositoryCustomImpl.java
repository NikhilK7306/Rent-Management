package com.rentms.repository;

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
                    .append("LOWER(r.tenant.full_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(r.tenant.mobile_number) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(r.property.property_code) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(r.property.property_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
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
}