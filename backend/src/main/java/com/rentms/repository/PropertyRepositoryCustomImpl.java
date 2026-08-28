package com.rentms.repository;

import com.rentms.entity.Property;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryCustomImpl implements PropertyRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<Property> searchProperties(String search, Property.Status status, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (")
                    .append("LOWER(p.property_name) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(p.property_code) LIKE LOWER(CONCAT('%', ?, '%')) ")
                    .append("OR LOWER(p.address) LIKE LOWER(CONCAT('%', ?, '%'))")
                    .append(")");
            params.add(search);
            params.add(search);
            params.add(search);
        }

        if (status != null) {
            whereClause.append(" AND p.status = ?");
            params.add(status.name());
        }

        String countSql = "SELECT COUNT(*) FROM properties p " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        StringBuilder dataSql = new StringBuilder("SELECT * FROM properties p ")
                .append(whereClause)
                .append(" ORDER BY p.created_at DESC");

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), Property.class);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Property> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Property> findAllByStatus(Property.Status status, Pageable pageable) {
        String whereClause = "WHERE 1=1";
        List<Object> params = new ArrayList<>();

        if (status != null) {
            whereClause += " AND p.status = ?";
            params.add(status.name());
        }

        String countSql = "SELECT COUNT(*) FROM properties p " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        String dataSql = "SELECT * FROM properties p " + whereClause + " ORDER BY p.created_at DESC";
        Query dataQuery = entityManager.createNativeQuery(dataSql, Property.class);
        for (int i = 0; i < params.size(); i++) {
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Property> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}