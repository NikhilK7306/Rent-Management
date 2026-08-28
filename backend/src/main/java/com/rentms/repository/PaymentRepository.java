package com.rentms.repository;

import com.rentms.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    Optional<Payment> findByReferenceNumber(String referenceNumber);

    List<Payment> findByRentId(Long rentId);

    Page<Payment> findByRentId(Long rentId, Pageable pageable);

    List<Payment> findByStatus(Payment.Status status);

    Page<Payment> findByStatus(Payment.Status status, Pageable pageable);

    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);

    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.rent.id = :rentId")
    BigDecimal sumAmountByRentId(@Param("rentId") Long rentId);
}