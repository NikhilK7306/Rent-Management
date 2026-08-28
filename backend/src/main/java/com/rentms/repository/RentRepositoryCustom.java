package com.rentms.repository;

import com.rentms.entity.Rent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentRepositoryCustom {
    Page<Rent> searchRents(String search, Rent.Status status, Integer month, Integer year, String overdue, Pageable pageable);
}