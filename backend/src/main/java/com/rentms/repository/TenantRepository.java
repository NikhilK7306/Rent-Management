package com.rentms.repository;

import com.rentms.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long>, TenantRepositoryCustom {

    Optional<Tenant> findByMobileNumber(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<Tenant> findByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndStatus(Long propertyId, Tenant.Status status);

    Optional<Tenant> findByPropertyIdAndStatus(Long propertyId, Tenant.Status status);

    List<Tenant> findByStatusAndPropertyIdIsNotNull(Tenant.Status status);
}