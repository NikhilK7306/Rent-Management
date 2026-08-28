package com.rentms.service;

import com.rentms.dto.property.PropertyRequest;
import com.rentms.dto.property.PropertyResponse;
import com.rentms.dto.property.PropertyStatusRequest;
import com.rentms.entity.Property;
import com.rentms.entity.Tenant;
import com.rentms.exception.PropertyNotFoundException;
import com.rentms.exception.DuplicatePropertyCodeException;
import com.rentms.repository.PropertyRepository;
import com.rentms.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public PropertyResponse createProperty(PropertyRequest request) {
        log.info("Creating property with code: {}", request.getPropertyCode());

        if (propertyRepository.existsByPropertyCode(request.getPropertyCode())) {
            log.warn("Property code already exists: {}", request.getPropertyCode());
            throw new DuplicatePropertyCodeException("Property code already exists: " + request.getPropertyCode());
        }

        Property property = Property.builder()
                .propertyName(request.getPropertyName())
                .propertyCode(request.getPropertyCode())
                .propertyType(request.getPropertyType())
                .address(request.getAddress())
                .description(request.getDescription())
                .monthlyRent(request.getMonthlyRent())
                .status(Property.Status.ACTIVE)
                .build();

        Property saved = propertyRepository.save(property);
        log.info("Property created with id: {}", saved.getId());
        return PropertyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAllProperties(String search, Property.Status status, Pageable pageable) {
        log.debug("Fetching properties with search: {}, status: {}", search, status);
        Page<Property> properties = propertyRepository.searchProperties(search, status, pageable);
        return properties.map(this::mapToResponseWithTenant);
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(Long id) {
        log.debug("Fetching property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Property not found with id: {}", id);
                    return new PropertyNotFoundException("Property not found with id: " + id);
                });
        return mapToResponseWithTenant(property);
    }

    @Transactional
    public PropertyResponse updateProperty(Long id, PropertyRequest request) {
        log.info("Updating property with id: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Property not found with id: {}", id);
                    return new PropertyNotFoundException("Property not found with id: " + id);
                });

        if (!property.getPropertyCode().equals(request.getPropertyCode())
                && propertyRepository.existsByPropertyCode(request.getPropertyCode())) {
            log.warn("Property code already exists: {}", request.getPropertyCode());
            throw new DuplicatePropertyCodeException("Property code already exists: " + request.getPropertyCode());
        }

        property.setPropertyName(request.getPropertyName());
        property.setPropertyCode(request.getPropertyCode());
        property.setPropertyType(request.getPropertyType());
        property.setAddress(request.getAddress());
        property.setDescription(request.getDescription());
        property.setMonthlyRent(request.getMonthlyRent());

        Property updated = propertyRepository.save(property);
        log.info("Property updated with id: {}", updated.getId());
        return mapToResponseWithTenant(updated);
    }

    @Transactional
    public PropertyResponse updatePropertyStatus(Long id, PropertyStatusRequest request) {
        log.info("Updating property status for id: {} to {}", id, request.getStatus());

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Property not found with id: {}", id);
                    return new PropertyNotFoundException("Property not found with id: " + id);
                });

        property.setStatus(request.getStatus());
        Property updated = propertyRepository.save(property);
        log.info("Property status updated for id: {}", updated.getId());
        return mapToResponseWithTenant(updated);
    }

    private PropertyResponse mapToResponseWithTenant(Property property) {
        Tenant tenant = null;
        if (property.getId() != null) {
            tenant = tenantRepository.findByPropertyIdAndStatus(property.getId(), Tenant.Status.ACTIVE).orElse(null);
        }
        return PropertyResponse.from(property, tenant);
    }
}