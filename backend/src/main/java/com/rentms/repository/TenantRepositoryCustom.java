package com.rentms.repository;

import com.rentms.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantRepositoryCustom {
    Page<Tenant> searchTenants(String search, Tenant.Status status, Pageable pageable);
}