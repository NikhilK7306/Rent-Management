package com.rentms.repository;

import com.rentms.entity.Rent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentRepository extends JpaRepository<Rent, Long>, RentRepositoryCustom {

    Optional<Rent> findByTenantIdAndPropertyIdAndRentMonthAndRentYear(
            Long tenantId, Long propertyId, Integer rentMonth, Integer rentYear);

    boolean existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
            Long tenantId, Long propertyId, Integer rentMonth, Integer rentYear);

    @Query("SELECT r FROM Rent r WHERE r.tenant.id = :tenantId ORDER BY r.rentYear DESC, r.rentMonth DESC")
    List<Rent> findByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT r FROM Rent r WHERE r.property.id = :propertyId ORDER BY r.rentYear DESC, r.rentMonth DESC")
    List<Rent> findByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT r FROM Rent r WHERE r.dueDate BETWEEN :startDate AND :endDate AND r.status IN :statuses")
    List<Rent> findByDueDateBetweenAndStatusIn(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("statuses") List<Rent.Status> statuses);

    @Query("SELECT r FROM Rent r WHERE r.dueDate < :today AND r.status != :paidStatus")
    List<Rent> findOverdueRents(@Param("today") LocalDate today, @Param("paidStatus") Rent.Status paidStatus);

    @Query("SELECT r FROM Rent r WHERE r.status = :status")
    List<Rent> findByStatus(@Param("status") Rent.Status status);

    @Query("SELECT new Object[]{r.tenant.id, r.tenant.fullName, COUNT(r), SUM(r.monthly_rent - COALESCE(r.paid_amount, 0))} " +
           "FROM Rent r WHERE r.status IN (:statuses) GROUP BY r.tenant.id, r.tenant.fullName HAVING SUM(r.monthly_rent - COALESCE(r.paid_amount, 0)) > 0")
    List<Object[]> getAggregatedOutstandingByTenant(@Param("statuses") List<Rent.Status> statuses);
}