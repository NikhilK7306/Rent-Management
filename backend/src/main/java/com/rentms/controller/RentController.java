package com.rentms.controller;

import com.rentms.dto.rent.BulkRentRequest;
import com.rentms.dto.rent.RentPageResponse;
import com.rentms.dto.rent.RentRequest;
import com.rentms.dto.rent.RentResponse;
import com.rentms.dto.rent.RentStatusRequest;
import com.rentms.dto.rent.TotalOutstandingRequest;
import com.rentms.entity.Rent;
import com.rentms.service.RentService;
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
@RequestMapping("/api/admin/rents")
@RequiredArgsConstructor
@Slf4j
public class RentController {

    private final RentService rentService;

    @PostMapping
    public ResponseEntity<RentResponse> createRent(@Valid @RequestBody RentRequest request) {
        log.info("POST /api/admin/rents - Create rent request received");
        RentResponse response = rentService.createRent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkRentRequest.BulkRentResult> createHistoricalRents(@Valid @RequestBody BulkRentRequest request) {
        log.info("POST /api/admin/rents/bulk - Create historical rents request received");
        BulkRentRequest.BulkRentResult response = rentService.createHistoricalRents(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/outstanding")
    public ResponseEntity<TotalOutstandingRequest.TotalOutstandingResult> createTotalOutstandingRent(@Valid @RequestBody TotalOutstandingRequest request) {
        log.info("POST /api/admin/rents/outstanding - Create total outstanding rent request received");
        TotalOutstandingRequest.TotalOutstandingResult response = rentService.createTotalOutstandingRent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<RentPageResponse<RentResponse>> getAllRents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Rent.Status status,
            @RequestParam(required = false) String overdue,
            @PageableDefault(page = 0, size = 10, sort = "rentYear,rentMonth") Pageable pageable) {
        log.debug("GET /api/admin/rents - search: {}, month: {}, year: {}, status: {}, overdue: {}", search, month, year, status, overdue);
        Page<RentResponse> rents = rentService.getAllRents(search, month, year, status, overdue, pageable);
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

    @GetMapping("/{id}")
    public ResponseEntity<RentResponse> getRentById(@PathVariable Long id) {
        log.debug("GET /api/admin/rents/{}", id);
        RentResponse response = rentService.getRentById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RentResponse> updateRentStatus(@PathVariable Long id, @Valid @RequestBody RentStatusRequest request) {
        log.info("PATCH /api/admin/rents/{}/status - Status update request received", id);
        RentResponse response = rentService.updateRentStatus(id, request);
        return ResponseEntity.ok(response);
    }
}