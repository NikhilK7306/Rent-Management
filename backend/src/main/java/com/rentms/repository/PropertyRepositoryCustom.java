package com.rentms.repository;

import com.rentms.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PropertyRepositoryCustom {
    Page<Property> searchProperties(String search, Property.Status status, Pageable pageable);
    Page<Property> findAllByStatus(Property.Status status, Pageable pageable);
}