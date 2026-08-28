package com.rentms.controller;

import com.rentms.dto.tenant.TenantPageResponse;
import com.rentms.dto.tenant.TenantPropertyRequest;
import com.rentms.dto.tenant.TenantRequest;
import com.rentms.dto.tenant.TenantResponse;
import com.rentms.dto.tenant.TenantStatusRequest;
import com.rentms.entity.Tenant;
import com.rentms.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantRequest request) {
        log.info("POST /api/admin/tenants - Create tenant request received");
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TenantPageResponse<TenantResponse>> getAllTenants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Tenant.Status status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {
        log.debug("GET /api/admin/tenants - search: {}, status: {}", search, status);
        Page<TenantResponse> tenants = tenantService.getAllTenants(search, status, pageable);
        TenantPageResponse<TenantResponse> response = convertToPageResponse(tenants);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long id) {
        log.debug("GET /api/admin/tenants/{}", id);
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        log.info("PUT /api/admin/tenants/{} - Update tenant request received", id);
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TenantResponse> updateTenantStatus(@PathVariable Long id, @Valid @RequestBody TenantStatusRequest request) {
        log.info("PATCH /api/admin/tenants/{}/status - Status update request received", id);
        TenantResponse response = tenantService.updateTenantStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tenantId}/property")
    public ResponseEntity<TenantResponse> assignProperty(@PathVariable Long tenantId, @Valid @RequestBody TenantPropertyRequest request) {
        log.info("PATCH /api/admin/tenants/{}/property - Assign property request received", tenantId);
        TenantResponse response = tenantService.assignProperty(tenantId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tenantId}/property")
    public ResponseEntity<TenantResponse> unassignProperty(@PathVariable Long tenantId) {
        log.info("DELETE /api/admin/tenants/{}/property - Unassign property request received", tenantId);
        TenantResponse response = tenantService.unassignProperty(tenantId);
        return ResponseEntity.ok(response);
    }

    private <T> TenantPageResponse<T> convertToPageResponse(Page<T> page) {
        return TenantPageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
}