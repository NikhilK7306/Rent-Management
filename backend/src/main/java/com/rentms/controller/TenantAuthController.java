package com.rentms.controller;

import com.rentms.dto.tenantauth.TenantMeResponse;
import com.rentms.entity.User;
import com.rentms.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
@Slf4j
public class TenantAuthController {

    private final TenantService tenantService;

    @GetMapping("/me")
    public ResponseEntity<TenantMeResponse> getCurrentTenant(@AuthenticationPrincipal User user) {
        log.debug("GET /api/tenant/me - Fetching current tenant for user: {}", user.getMobileNumber());

        TenantMeResponse response = tenantService.getTenantMeByMobileNumber(user.getMobileNumber());
        
        return ResponseEntity.ok(response);
    }
}