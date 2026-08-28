package com.rentms.controller;

import com.rentms.dto.property.PropertyPageResponse;
import com.rentms.dto.property.PropertyRequest;
import com.rentms.dto.property.PropertyResponse;
import com.rentms.dto.property.PropertyStatusRequest;
import com.rentms.entity.Property;
import com.rentms.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody PropertyRequest request) {
        log.info("POST /api/admin/properties - Create property request received");
        PropertyResponse response = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PropertyPageResponse<PropertyResponse>> getAllProperties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Property.Status status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {
        log.debug("GET /api/admin/properties - search: {}, status: {}", search, status);
        Page<PropertyResponse> properties = propertyService.getAllProperties(search, status, pageable);
        PropertyPageResponse<PropertyResponse> response = convertToPageResponse(properties);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        log.debug("GET /api/admin/properties/{}", id);
        PropertyResponse response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        log.info("PUT /api/admin/properties/{} - Update property request received", id);
        PropertyResponse response = propertyService.updateProperty(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PropertyResponse> updatePropertyStatus(@PathVariable Long id, @Valid @RequestBody PropertyStatusRequest request) {
        log.info("PATCH /api/admin/properties/{}/status - Status update request received", id);
        PropertyResponse response = propertyService.updatePropertyStatus(id, request);
        return ResponseEntity.ok(response);
    }

    private <T> PropertyPageResponse<T> convertToPageResponse(Page<T> page) {
        return PropertyPageResponse.<T>builder()
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