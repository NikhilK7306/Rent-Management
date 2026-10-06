package com.rentms.controller;

import com.rentms.dto.rent.RentPageResponse;
import com.rentms.dto.rent.RentResponse;
import com.rentms.entity.User;
import com.rentms.service.RentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/rents")
@RequiredArgsConstructor
@Slf4j
public class TenantRentController {

    private final RentService rentService;

    @GetMapping
    public ResponseEntity<RentPageResponse<RentResponse>> getMyRents(
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 10, sort = "rentYear,rentMonth") Pageable pageable) {
        log.debug("GET /api/tenant/rents - Fetching rents for tenant: {}", user.getMobileNumber());
        Page<RentResponse> rents = rentService.getRentsByTenantMobileNumber(user.getMobileNumber(), pageable);
        
        RentPageResponse<RentResponse> response = new RentPageResponse<>();
        response.setContent(rents.getContent());
        response.setTotalElements(rents.getTotalElements());
        response.setTotalPages(rents.getTotalPages());
        response.setSize(rents.getSize());
        response.setNumber(rents.getNumber());
        response.setFirst(rents.isFirst());
        response.setLast(rents.isLast());
        response.setNumberOfElements(rents.getNumberOfElements());
        response.setEmpty(rents.isEmpty());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<RentResponse> getCurrentMonthRent(@AuthenticationPrincipal User user) {
        log.debug("GET /api/tenant/rents/current - Fetching current month rent for tenant: {}", user.getMobileNumber());
        // Get the first (most recent) rent record
        Page<RentResponse> rents = rentService.getRentsByTenantMobileNumber(user.getMobileNumber(), 
                org.springframework.data.domain.PageRequest.of(0, 1));
        
        if (rents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(rents.getContent().get(0));
    }
}