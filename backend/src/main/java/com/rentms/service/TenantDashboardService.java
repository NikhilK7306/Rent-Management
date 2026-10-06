package com.rentms.service;

import com.rentms.dto.tenantauth.TenantDashboardResponse;
import com.rentms.entity.Property;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import com.rentms.repository.PropertyRepository;
import com.rentms.repository.RentRepository;
import com.rentms.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantDashboardService {

    private final TenantRepository tenantRepository;
    private final RentRepository rentRepository;
    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public TenantDashboardResponse getDashboardForTenant(String mobileNumber) {
        log.debug("Building dashboard for tenant: {}", mobileNumber);
        
        Tenant tenant = tenantRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new com.rentms.exception.TenantNotFoundException("Tenant not found for mobile number: " + mobileNumber));
        
        TenantDashboardResponse response = TenantDashboardResponse.builder()
                .tenantName(tenant.getFullName())
                .mobileNumber(tenant.getMobileNumber())
                .email(tenant.getEmail())
                .build();
        
        // Add property info if assigned
        if (tenant.getPropertyId() != null) {
            Property property = propertyRepository.findById(tenant.getPropertyId()).orElse(null);
            if (property != null) {
                response.setProperty(TenantDashboardResponse.PropertyInfo.builder()
                        .id(property.getId())
                        .propertyCode(property.getPropertyCode())
                        .propertyName(property.getPropertyName())
                        .propertyType(property.getPropertyType().name())
                        .address(property.getAddress())
                        .monthlyRent(property.getMonthlyRent() != null ? property.getMonthlyRent().toString() : null)
                        .build());
            }
        }
        
        // Get current month rent
        YearMonth currentMonth = YearMonth.now();
        Optional<Rent> currentRent = rentRepository.findByTenantIdAndPropertyIdAndRentMonthAndRentYear(
                tenant.getId(), tenant.getPropertyId(), currentMonth.getMonthValue(), currentMonth.getYear());
        
        if (currentRent.isPresent()) {
            Rent rent = currentRent.get();
            BigDecimal monthlyRent = rent.getMonthlyRent();
            BigDecimal paidAmount = rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal outstanding = monthlyRent.subtract(paidAmount);
            
            response.setRentSummary(TenantDashboardResponse.RentSummary.builder()
                    .currentMonthRent(monthlyRent.toString())
                    .paidAmount(paidAmount.toString())
                    .outstandingAmount(outstanding.toString())
                    .status(rent.getStatus().name())
                    .rentMonth(rent.getRentMonth())
                    .rentYear(rent.getRentYear())
                    .build());
        } else if (tenant.getPropertyId() != null) {
            // No rent record for current month, but tenant has a property
            Property property = propertyRepository.findById(tenant.getPropertyId()).orElse(null);
            if (property != null && property.getMonthlyRent() != null) {
                response.setRentSummary(TenantDashboardResponse.RentSummary.builder()
                        .currentMonthRent(property.getMonthlyRent().toString())
                        .paidAmount("0")
                        .outstandingAmount(property.getMonthlyRent().toString())
                        .status("PENDING")
                        .rentMonth(currentMonth.getMonthValue())
                        .rentYear(currentMonth.getYear())
                        .build());
            }
        }
        
        return response;
    }
}