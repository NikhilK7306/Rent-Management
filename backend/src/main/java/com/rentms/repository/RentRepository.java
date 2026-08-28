package com.rentms.repository;

import com.rentms.entity.Rent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentRepository extends JpaRepository<Rent, Long>, RentRepositoryCustom {

    Optional<Rent> findByTenantIdAndPropertyIdAndRentMonthAndRentYear(
            Long tenantId, Long propertyId, Integer rentMonth, Integer rentYear);

    boolean existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
            Long tenantId, Long propertyId, Integer rentMonth, Integer rentYear);
}