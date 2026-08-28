package com.rentms.service;

import com.rentms.dto.tenant.TenantPropertyRequest;
import com.rentms.dto.tenant.TenantRequest;
import com.rentms.dto.tenant.TenantResponse;
import com.rentms.dto.tenant.TenantStatusRequest;
import com.rentms.entity.Property;
import com.rentms.entity.Tenant;
import com.rentms.exception.DuplicateMobileNumberException;
import com.rentms.exception.PropertyNotFoundException;
import com.rentms.exception.TenantAssignmentException;
import com.rentms.exception.TenantNotFoundException;
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
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final RentService rentService;

    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        log.info("Creating tenant with mobile number: {}", request.getMobileNumber());

        if (tenantRepository.existsByMobileNumber(request.getMobileNumber())) {
            log.warn("Mobile number already exists: {}", request.getMobileNumber());
            throw new DuplicateMobileNumberException("Mobile number already exists: " + request.getMobileNumber());
        }

        Tenant tenant = Tenant.builder()
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(Tenant.Status.ACTIVE)
                .propertyId(null)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant created with id: {}", saved.getId());
        return TenantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(String search, Tenant.Status status, Pageable pageable) {
        log.debug("Fetching tenants with search: {}, status: {}", search, status);
        Page<Tenant> tenants = tenantRepository.searchTenants(search, status, pageable);
        return tenants.map(this::mapToResponseWithProperty);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(Long id) {
        log.debug("Fetching tenant with id: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", id);
                    return new TenantNotFoundException("Tenant not found with id: " + id);
                });
        return mapToResponseWithProperty(tenant);
    }

    @Transactional
    public TenantResponse updateTenant(Long id, TenantRequest request) {
        log.info("Updating tenant with id: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", id);
                    return new TenantNotFoundException("Tenant not found with id: " + id);
                });

        if (!tenant.getMobileNumber().equals(request.getMobileNumber())
                && tenantRepository.existsByMobileNumber(request.getMobileNumber())) {
            log.warn("Mobile number already exists: {}", request.getMobileNumber());
            throw new DuplicateMobileNumberException("Mobile number already exists: " + request.getMobileNumber());
        }

        tenant.setFullName(request.getFullName());
        tenant.setMobileNumber(request.getMobileNumber());
        tenant.setEmail(request.getEmail());
        tenant.setAddress(request.getAddress());

        Tenant updated = tenantRepository.save(tenant);
        log.info("Tenant updated with id: {}", updated.getId());
        return mapToResponseWithProperty(updated);
    }

    @Transactional
    public TenantResponse updateTenantStatus(Long id, TenantStatusRequest request) {
        log.info("Updating tenant status for id: {} to {}", id, request.getStatus());

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", id);
                    return new TenantNotFoundException("Tenant not found with id: " + id);
                });

        tenant.setStatus(request.getStatus());
        Tenant updated = tenantRepository.save(tenant);
        log.info("Tenant status updated for id: {}", updated.getId());
        return mapToResponseWithProperty(updated);
    }

    @Transactional
    public TenantResponse assignProperty(Long tenantId, TenantPropertyRequest request) {
        log.info("Assigning property {} to tenant {}", request.getPropertyId(), tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", tenantId);
                    return new TenantNotFoundException("Tenant not found with id: " + tenantId);
                });

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new TenantAssignmentException("Cannot assign property to inactive tenant");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> {
                    log.warn("Property not found with id: {}", request.getPropertyId());
                    return new PropertyNotFoundException("Property not found with id: " + request.getPropertyId());
                });

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new TenantAssignmentException("Cannot assign tenant to inactive property");
        }

        if (tenantRepository.existsByPropertyIdAndStatus(property.getId(), Tenant.Status.ACTIVE)) {
            throw new TenantAssignmentException("Property " + property.getPropertyCode() + " is already assigned to another active tenant");
        }

        tenant.setPropertyId(property.getId());
        Tenant updated = tenantRepository.save(tenant);
        log.info("Property assigned to tenant. Tenant id: {}, Property id: {}", updated.getId(), property.getId());

        // Generate current month rent for this assignment
        rentService.generateCurrentMonthRentForAssignment(updated.getId(), property.getId());

        return mapToResponseWithProperty(updated);
    }

    @Transactional
    public TenantResponse unassignProperty(Long tenantId) {
        log.info("Unassigning property from tenant {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", tenantId);
                    return new TenantNotFoundException("Tenant not found with id: " + tenantId);
                });

        tenant.setPropertyId(null);
        Tenant updated = tenantRepository.save(tenant);
        log.info("Property unassigned from tenant id: {}", updated.getId());
        return mapToResponseWithProperty(updated);
    }

    @Transactional
    public TenantResponse changeProperty(Long tenantId, TenantPropertyRequest request) {
        log.info("Changing property for tenant {} to property {}", tenantId, request.getPropertyId());

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with id: {}", tenantId);
                    return new TenantNotFoundException("Tenant not found with id: " + tenantId);
                });

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new TenantAssignmentException("Cannot change property for inactive tenant");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> {
                    log.warn("Property not found with id: {}", request.getPropertyId());
                    return new PropertyNotFoundException("Property not found with id: " + request.getPropertyId());
                });

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new TenantAssignmentException("Cannot assign tenant to inactive property");
        }

        if (tenantRepository.existsByPropertyIdAndStatus(property.getId(), Tenant.Status.ACTIVE)) {
            throw new TenantAssignmentException("Property " + property.getPropertyCode() + " is already assigned to another active tenant");
        }

        tenant.setPropertyId(property.getId());
        Tenant updated = tenantRepository.save(tenant);
        log.info("Property changed for tenant. Tenant id: {}, New property id: {}", updated.getId(), property.getId());

        // Generate current month rent for the new property assignment
        rentService.generateCurrentMonthRentForAssignment(updated.getId(), property.getId());

        return mapToResponseWithProperty(updated);
    }

    private TenantResponse mapToResponseWithProperty(Tenant tenant) {
        if (tenant.getPropertyId() != null) {
            Property property = propertyRepository.findById(tenant.getPropertyId()).orElse(null);
            if (property != null) {
                TenantResponse.PropertyInfo propertyInfo = TenantResponse.PropertyInfo.builder()
                        .id(property.getId())
                        .propertyCode(property.getPropertyCode())
                        .propertyName(property.getPropertyName())
                        .propertyType(property.getPropertyType().name())
                        .build();
                return TenantResponse.from(tenant, propertyInfo);
            }
        }
        return TenantResponse.from(tenant);
    }
}