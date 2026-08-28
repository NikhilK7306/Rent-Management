package com.rentms.repository;

import com.rentms.entity.Tenant;
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
public class TenantRepositoryCustomImpl implements TenantRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<Tenant> searchTenants(String search, Tenant.Status status, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (")
                    .append("LOWER(t.full_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(t.mobile_number) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(t.email) LIKE LOWER(CONCAT('%', ?, '%'))")
                    .append(")");
            params.add(search);
            params.add(search);
            params.add(search);
        }

        if (status != null) {
            whereClause.append(" AND t.status = ?");
            params.add(status.name());
        }

        String countSql = "SELECT COUNT(*) FROM tenants t " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        StringBuilder dataSql = new StringBuilder("SELECT * FROM tenants t ")
                .append(whereClause)
                .append(" ORDER BY t.created_at DESC");

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), Tenant.class);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Tenant> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}