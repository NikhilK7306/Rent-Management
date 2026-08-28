package com.rentms.repository;

import com.rentms.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, PropertyRepositoryCustom {

    Optional<Property> findByPropertyCode(String propertyCode);

    boolean existsByPropertyCode(String propertyCode);
}