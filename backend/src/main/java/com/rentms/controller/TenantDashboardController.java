package com.rentms.controller;

import com.rentms.dto.tenantauth.TenantDashboardResponse;
import com.rentms.entity.User;
import com.rentms.service.TenantDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/dashboard")
@RequiredArgsConstructor
@Slf4j
public class TenantDashboardController {

    private final TenantDashboardService tenantDashboardService;

    @GetMapping
    public ResponseEntity<TenantDashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        log.debug("GET /api/tenant/dashboard - Fetching dashboard for tenant: {}", user.getMobileNumber());
        TenantDashboardResponse response = tenantDashboardService.getDashboardForTenant(user.getMobileNumber());
        return ResponseEntity.ok(response);
    }
}