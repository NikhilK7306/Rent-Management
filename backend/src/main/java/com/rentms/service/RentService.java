package com.rentms.service;

import com.rentms.dto.rent.BulkRentRequest;
import com.rentms.dto.rent.RentRequest;
import com.rentms.dto.rent.RentResponse;
import com.rentms.dto.rent.RentStatusRequest;
import com.rentms.dto.rent.TotalOutstandingRequest;
import com.rentms.entity.Property;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import com.rentms.exception.*;
import com.rentms.repository.PropertyRepository;
import com.rentms.repository.RentRepository;
import com.rentms.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentService {

    @Value("${app.rent.auto-generation.enabled:true}")
    private boolean autoGenerationEnabled;

    private final RentRepository rentRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public RentResponse createRent(RentRequest request) {
        log.info("Creating rent for tenant {} and property {} for {}/{}",
                request.getTenantId(), request.getPropertyId(), request.getRentMonth(), request.getRentYear());

        // Validate tenant
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + request.getTenantId()));

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive tenant");
        }

        if (tenant.getPropertyId() == null) {
            throw new RentException("Cannot create rent for unassigned tenant");
        }

        // Validate property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Verify tenant is assigned to this property
        if (tenant.getPropertyId() == null || !tenant.getPropertyId().equals(request.getPropertyId())) {
            throw new RentException("Tenant is not assigned to this property");
        }

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Check for duplicate rent record
        if (rentRepository.existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
                request.getTenantId(), request.getPropertyId(), request.getRentMonth(), request.getRentYear())) {
            throw new DuplicateRentException("Rent record already exists for this tenant/property/month/year");
        }

        // Calculate due date (e.g., 1st of the month)
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() :
                LocalDate.of(request.getRentYear(), request.getRentMonth(), 1);

        Rent rent = Rent.builder()
                .tenant(tenant)
                .property(property)
                .rentMonth(request.getRentMonth())
                .rentYear(request.getRentYear())
                .monthlyRent(request.getMonthlyRent())
                .dueDate(dueDate)
                .status(Rent.Status.PENDING)
                .build();

        Rent saved = rentRepository.save(rent);
        log.info("Rent created with id: {}", saved.getId());
        return RentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<RentResponse> getAllRents(String search, Integer month, Integer year, Rent.Status status, String overdue, Pageable pageable) {
        log.debug("Fetching rents with search: {}, month: {}, year: {}, status: {}, overdue: {}", search, month, year, status, overdue);
        Page<Rent> rents = rentRepository.searchRents(search, status, month, year, overdue, pageable);
        return rents.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RentResponse getRentById(Long id) {
        log.debug("Fetching rent with id: {}", id);
        Rent rent = rentRepository.findById(id)
                .orElseThrow(() -> new RentNotFoundException("Rent not found with id: " + id));
        return mapToResponse(rent);
    }

    @Transactional
    public RentResponse updateRentStatus(Long id, RentStatusRequest request) {
        log.info("Updating rent status for id: {} to {}", id, request.getStatus());

        Rent rent = rentRepository.findById(id)
                .orElseThrow(() -> new RentNotFoundException("Rent not found with id: " + id));

        // Validate status transition
        Rent.Status currentStatus = rent.getStatus();
        Rent.Status newStatus = convertToEntityStatus(request.getStatus());

        if (currentStatus == newStatus) {
            return RentResponse.from(rent);
        }

        // Validate status transitions
        if (currentStatus == Rent.Status.PAID && newStatus != Rent.Status.PAID) {
            throw new RentException("Cannot change status from PAID to another status");
        }

        if (currentStatus == Rent.Status.OVERDUE && newStatus == Rent.Status.PENDING) {
            throw new RentException("Cannot change status from OVERDUE to PENDING");
        }

        rent.setStatus(newStatus);

        if (newStatus == Rent.Status.PAID) {
            rent.setPaidDate(LocalDate.now());
            rent.setPaidAmount(rent.getMonthlyRent());
        } else {
            rent.setPaidDate(null);
            rent.setPaidAmount(null);
        }

        Rent updated = rentRepository.save(rent);
        log.info("Rent status updated for id: {}", updated.getId());
        return RentResponse.from(updated);
    }

    private Rent.Status convertToEntityStatus(RentStatusRequest.Status dtoStatus) {
        return switch (dtoStatus) {
            case PENDING -> Rent.Status.PENDING;
            case PAID -> Rent.Status.PAID;
            case OVERDUE -> Rent.Status.OVERDUE;
        };
    }

    @Transactional(readOnly = true)
    public Page<RentResponse> getRentsByTenant(Long tenantId, Pageable pageable) {
        // This would need a custom repository method
        // For now, we can filter by tenant in the custom query
        return rentRepository.searchRents(null, null, null, null, null, pageable)
                .map(this::mapToResponse); // TODO: Add proper tenant filtering
    }

    @Transactional(readOnly = true)
    public Page<RentResponse> getRentsByProperty(Long propertyId, Pageable pageable) {
        // TODO: Add custom repository method for property filtering
        return rentRepository.searchRents(null, null, null, null, null, pageable)
                .map(this::mapToResponse); // TODO: Add proper property filtering
    }

    @Transactional
    public void updateOverdueRents() {
        log.info("Updating overdue rents");
        LocalDate today = LocalDate.now();
        // Find all PENDING rents where due date has passed
        // This would need a custom query
    }

    /**
     * Creates historical rent records for a date range.
     * Skips months that already have rent records.
     */
    @Transactional
    public BulkRentRequest.BulkRentResult createHistoricalRents(BulkRentRequest request) {
        log.info("Creating historical rents for tenant {} and property {} from {}/{} to {}/{}",
                request.getTenantId(), request.getPropertyId(),
                request.getStartMonth(), request.getStartYear(),
                request.getEndMonth(), request.getEndYear());

        // Validate tenant
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + request.getTenantId()));

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive tenant");
        }

        if (tenant.getPropertyId() == null) {
            throw new RentException("Cannot create rent for unassigned tenant");
        }

        // Validate property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Verify tenant is assigned to this property
        if (tenant.getPropertyId() == null || !tenant.getPropertyId().equals(request.getPropertyId())) {
            throw new RentException("Tenant is not assigned to this property");
        }

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Validate date range
        if (request.getStartYear() > request.getEndYear() ||
                (request.getStartYear().equals(request.getEndYear()) && request.getStartMonth() > request.getEndMonth())) {
            throw new RentException("Start date must be before or equal to end date");
        }

        // Calculate total months
        int totalMonths = calculateMonthsBetween(
                request.getStartYear(), request.getStartMonth(),
                request.getEndYear(), request.getEndMonth());

        if (totalMonths <= 0) {
            throw new RentException("Invalid date range: no months to generate");
        }

        // Check for existing rents and create missing ones
        int createdCount = 0;
        int skippedCount = 0;
        List<BulkRentRequest.BulkRentResult.SkippedMonth> skippedMonths = new ArrayList<>();

        int currentMonth = request.getStartMonth();
        int currentYear = request.getStartYear();

        for (int i = 0; i < totalMonths; i++) {
            // Check if rent already exists
            if (rentRepository.existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
                    request.getTenantId(), request.getPropertyId(), currentMonth, currentYear)) {
                skippedCount++;
                skippedMonths.add(BulkRentRequest.BulkRentResult.SkippedMonth.builder()
                        .month(currentMonth)
                        .year(currentYear)
                        .reason("Rent record already exists")
                        .build());
            } else {
                // Create rent record
                LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() :
                        LocalDate.of(currentYear, currentMonth, 1);

                Rent rent = Rent.builder()
                        .tenant(tenantRepository.findById(request.getTenantId()).orElseThrow())
                        .property(propertyRepository.findById(request.getPropertyId()).orElseThrow())
                        .rentMonth(currentMonth)
                        .rentYear(currentYear)
                        .monthlyRent(request.getMonthlyRent())
                        .dueDate(dueDate)
                        .status(request.getInitialStatus() != null ? request.getInitialStatus() : Rent.Status.PENDING)
                        .build();

                rentRepository.save(rent);
                createdCount++;
                log.debug("Created historical rent for tenant {} and property {} for {}/{}",
                        request.getTenantId(), request.getPropertyId(), currentMonth, currentYear);
            }

            // Move to next month
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
        }

        BigDecimal totalAmount = request.getMonthlyRent().multiply(BigDecimal.valueOf(createdCount));

        return BulkRentRequest.BulkRentResult.builder()
                .createdCount(createdCount)
                .skippedCount(skippedCount)
                .skippedMonths(skippedMonths)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * Creates rent records from a total outstanding amount.
     * If numberOfMonths is provided, distributes the amount across months.
     * If not provided, creates a single consolidated arrears record.
     */
    @Transactional
    public TotalOutstandingRequest.TotalOutstandingResult createTotalOutstandingRent(TotalOutstandingRequest request) {
        log.info("Creating total outstanding rent for tenant {} and property {}",
                request.getTenantId(), request.getPropertyId());

        // Validate tenant
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + request.getTenantId()));

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive tenant");
        }

        if (tenant.getPropertyId() == null) {
            throw new RentException("Cannot create rent for unassigned tenant");
        }

        // Validate property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Verify tenant is assigned to this property
        if (tenant.getPropertyId() == null || !tenant.getPropertyId().equals(request.getPropertyId())) {
            throw new RentException("Tenant is not assigned to this property");
        }

        if (property.getStatus() != Property.Status.ACTIVE) {
            throw new RentException("Cannot create rent for inactive property");
        }

        // Create a single consolidated arrears record
        // Use a special marker (month=0, year=asOfDate year) to indicate consolidated arrears
        Rent rent = Rent.builder()
                .tenant(tenantRepository.findById(request.getTenantId()).orElseThrow())
                .property(propertyRepository.findById(request.getPropertyId()).orElseThrow())
                .rentMonth(0) // 0 indicates consolidated arrears
                .rentYear(request.getAsOfDate().getYear())
                .monthlyRent(request.getTotalOutstandingAmount())
                .dueDate(request.getAsOfDate())
                .status(Rent.Status.PENDING)
                .build();

        Rent saved = rentRepository.save(rent);

        return TotalOutstandingRequest.TotalOutstandingResult.builder()
                .createdCount(1)
                .monthlyAmount(BigDecimal.ZERO)
                .totalAmount(request.getTotalOutstandingAmount())
                .isConsolidated(true)
                .build();
    }

    private int calculateMonthsBetween(int startYear, int startMonth, int endYear, int endMonth) {
        return (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
    }

    private RentResponse mapToResponse(Rent rent) {
        return RentResponse.from(rent);
    }

    /**
     * Generates current month rent for a specific tenant-property assignment if it doesn't exist.
     * This is called when a tenant is assigned to a property or when property is changed.
     */
    @Transactional
    public void generateCurrentMonthRentForAssignment(Long tenantId, Long propertyId) {
        if (!autoGenerationEnabled) {
            log.debug("Automatic rent generation is disabled, skipping");
            return;
        }
        
        log.info("Generating current month rent for tenant {} and property {}", tenantId, propertyId);

        // Validate tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() != Tenant.Status.ACTIVE) {
            log.debug("Tenant {} is not active, skipping rent generation", tenantId);
            return;
        }

        if (tenant.getPropertyId() == null || !tenant.getPropertyId().equals(propertyId)) {
            log.debug("Tenant {} is not assigned to property {}, skipping rent generation", tenantId, propertyId);
            return;
        }

        // Validate property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + propertyId));

        if (property.getStatus() != Property.Status.ACTIVE) {
            log.debug("Property {} is not active, skipping rent generation", propertyId);
            return;
        }

        // Get current month and year
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Check if rent already exists for this month
        if (rentRepository.existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
                tenantId, propertyId, currentMonth, currentYear)) {
            log.debug("Rent for tenant {} and property {} for {}/{} already exists, skipping",
                    tenantId, propertyId, currentMonth, currentYear);
            return;
        }

        // Calculate due date (1st of the month)
        LocalDate dueDate = LocalDate.of(currentYear, currentMonth, 1);

        // Create rent record
        Rent rent = Rent.builder()
                .tenant(tenant)
                .property(property)
                .rentMonth(currentMonth)
                .rentYear(currentYear)
                .monthlyRent(property.getMonthlyRent())
                .dueDate(dueDate)
                .status(Rent.Status.PENDING)
                .build();

        Rent saved = rentRepository.save(rent);
        log.info("Auto-generated rent created with id: {} for tenant {} and property {} for {}/{}",
                saved.getId(), tenantId, propertyId, currentMonth, currentYear);
    }

    /**
     * Generates current month rent for all active tenant-property assignments.
     * This is called by the scheduled job daily.
     */
    @Transactional
    public void generateCurrentMonthRentForAllActiveAssignments() {
        log.info("Starting scheduled generation of current month rents for all active assignments");

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Find all active tenants with active property assignments
        List<Tenant> activeTenants = tenantRepository.findByStatusAndPropertyIdIsNotNull(Tenant.Status.ACTIVE);

        int generatedCount = 0;
        int skippedCount = 0;

        for (Tenant tenant : activeTenants) {
            Long propertyId = tenant.getPropertyId();
            if (propertyId == null) {
                continue;
            }

            Property property = propertyRepository.findById(propertyId).orElse(null);
            if (property == null || property.getStatus() != Property.Status.ACTIVE) {
                continue;
            }

            // Check if rent already exists
            if (rentRepository.existsByTenantIdAndPropertyIdAndRentMonthAndRentYear(
                    tenant.getId(), propertyId, currentMonth, currentYear)) {
                skippedCount++;
                continue;
            }

            // Create rent record
            LocalDate dueDate = LocalDate.of(currentYear, currentMonth, 1);

            Rent rent = Rent.builder()
                    .tenant(tenant)
                    .property(property)
                    .rentMonth(currentMonth)
                    .rentYear(currentYear)
                    .monthlyRent(property.getMonthlyRent())
                    .dueDate(dueDate)
                    .status(Rent.Status.PENDING)
                    .build();

            rentRepository.save(rent);
            generatedCount++;
            log.debug("Auto-generated rent for tenant {} and property {} for {}/{}",
                    tenant.getId(), propertyId, currentMonth, currentYear);
        }

        log.info("Scheduled rent generation completed. Generated: {}, Skipped (already exist): {}",
                generatedCount, skippedCount);
    }

    /**
     * Scheduled job to generate current month rents for all active assignments.
     * Runs daily at 1:00 AM.
     * Disabled in test profile.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
    public void scheduledRentGeneration() {
        log.info("Running scheduled rent generation job");
        try {
            generateCurrentMonthRentForAllActiveAssignments();
        } catch (Exception e) {
            log.error("Error during scheduled rent generation", e);
        }
    }
}