package com.rentms.controller;

import com.rentms.dto.payment.PaymentPageResponse;
import com.rentms.dto.payment.PaymentRequest;
import com.rentms.dto.payment.PaymentResponse;
import com.rentms.entity.Payment;
import com.rentms.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/admin/payments - Create payment request received");
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaymentPageResponse<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long rentId,
            @RequestParam(required = false) Payment.Status status,
            @RequestParam(required = false) Payment.PaymentMethod paymentMethod,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "paymentDate,desc") Pageable pageable) {
        log.debug("GET /api/admin/payments - search: {}, rentId: {}, status: {}, paymentMethod: {}, startDate: {}, endDate: {}",
                search, rentId, status, paymentMethod, startDate, endDate);
        Page<PaymentResponse> payments = paymentService.getAllPayments(search, rentId, status, paymentMethod, startDate, endDate, pageable);
        PaymentPageResponse<PaymentResponse> response = new PaymentPageResponse<>();
        response.setContent(payments.getContent());
        response.setTotalElements(payments.getTotalElements());
        response.setTotalPages(payments.getTotalPages());
        response.setSize(payments.getSize());
        response.setNumber(payments.getNumber());
        response.setFirst(payments.isFirst());
        response.setLast(payments.isLast());
        response.setNumberOfElements(payments.getNumberOfElements());
        response.setEmpty(payments.isEmpty());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.debug("GET /api/admin/payments/{}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rent/{rentId}")
    public ResponseEntity<PaymentPageResponse<PaymentResponse>> getPaymentsByRent(
            @PathVariable Long rentId,
            @PageableDefault(page = 0, size = 10, sort = "paymentDate,desc") Pageable pageable) {
        log.debug("GET /api/admin/payments/rent/{}", rentId);
        Page<PaymentResponse> payments = paymentService.getPaymentsByRent(rentId, pageable);
        PaymentPageResponse<PaymentResponse> response = new PaymentPageResponse<>();
        response.setContent(payments.getContent());
        response.setTotalElements(payments.getTotalElements());
        response.setTotalPages(payments.getTotalPages());
        response.setSize(payments.getSize());
        response.setNumber(payments.getNumber());
        response.setFirst(payments.isFirst());
        response.setLast(payments.isLast());
        response.setNumberOfElements(payments.getNumberOfElements());
        response.setEmpty(payments.isEmpty());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        log.info("PUT /api/admin/payments/{} - Update payment request received", id);
        PaymentResponse response = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("DELETE /api/admin/payments/{} - Delete payment request received", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long id) {
        log.info("PATCH /api/admin/payments/{}/cancel - Cancel payment request received", id);
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }
}