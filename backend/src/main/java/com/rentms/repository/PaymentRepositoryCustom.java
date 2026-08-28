package com.rentms.repository;

import com.rentms.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PaymentRepositoryCustom {

    Page<Payment> searchPayments(
            String search,
            Long rentId,
            Payment.Status status,
            Payment.PaymentMethod paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
}