package com.rentms.service;

import com.rentms.dto.payment.PaymentRequest;
import com.rentms.dto.payment.PaymentResponse;
import com.rentms.entity.Payment;
import com.rentms.entity.Rent;
import com.rentms.exception.*;
import com.rentms.repository.PaymentRepository;
import com.rentms.repository.RentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentRepository rentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for rent ID: {}", request.getRentId());

        // Validate rent exists
        Rent rent = rentRepository.findById(request.getRentId())
                .orElseThrow(() -> new RentNotFoundException("Rent not found with id: " + request.getRentId()));

        // Validate tenant is active
        if (rent.getTenant().getStatus() != com.rentms.entity.Tenant.Status.ACTIVE) {
            throw new PaymentException("Cannot create payment for inactive tenant");
        }

        // Validate property is active
        if (rent.getProperty().getStatus() != com.rentms.entity.Property.Status.ACTIVE) {
            throw new PaymentException("Cannot create payment for inactive property");
        }

        // Calculate outstanding amount
        BigDecimal totalPaid = getTotalPaidForRent(rent.getId());
        BigDecimal outstanding = rent.getMonthlyRent().subtract(totalPaid);

        // Validate payment amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount must be greater than zero");
        }

        // Prevent overpayment unless explicitly allowed
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new PaymentException(
                    String.format("Payment amount (%.2f) exceeds outstanding amount (%.2f) for this rent",
                            request.getAmount(), outstanding)
            );
        }

        // Check for duplicate reference number
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().trim().isEmpty()) {
            Optional<Payment> existing = paymentRepository.findByReferenceNumber(request.getReferenceNumber().trim());
            if (existing.isPresent()) {
                throw new DuplicatePaymentException("Payment with reference number " + request.getReferenceNumber() + " already exists");
            }
        }

        // Validate payment date
        if (request.getPaymentDate().isAfter(LocalDate.now())) {
            throw new PaymentException("Payment date cannot be in the future");
        }

        // Create payment
        Payment payment = Payment.builder()
                .rent(rent)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(convertToEntityPaymentMethod(request.getPaymentMethod()))
                .referenceNumber(request.getReferenceNumber() != null ? request.getReferenceNumber().trim() : null)
                .notes(request.getNotes())
                .status(calculatePaymentStatus(request.getAmount(), outstanding))
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created with id: {} for rent: {}", saved.getId(), rent.getId());

        // Update rent status based on total payments
        updateRentStatusBasedOnPayments(rent);

        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(
            String search,
            Long rentId,
            Payment.Status status,
            Payment.PaymentMethod paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        log.debug("Fetching payments with search: {}, rentId: {}, status: {}, paymentMethod: {}, startDate: {}, endDate: {}",
                search, rentId, status, paymentMethod, startDate, endDate);
        return paymentRepository.searchPayments(search, rentId, status, paymentMethod, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.debug("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByRent(Long rentId, Pageable pageable) {
        log.debug("Fetching payments for rent id: {}", rentId);
        return paymentRepository.findByRentId(rentId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForRent(Long rentId) {
        BigDecimal total = paymentRepository.sumAmountByRentId(rentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingForRent(Long rentId) {
        Rent rent = rentRepository.findById(rentId)
                .orElseThrow(() -> new RentNotFoundException("Rent not found with id: " + rentId));
        BigDecimal totalPaid = getTotalPaidForRent(rentId);
        return rent.getMonthlyRent().subtract(totalPaid);
    }

    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        log.info("Updating payment with id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        Rent rent = payment.getRent();
        BigDecimal totalPaid = getTotalPaidForRent(rent.getId());
        BigDecimal outstanding = rent.getMonthlyRent().subtract(totalPaid.add(payment.getAmount()));

        // Validate payment amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount must be greater than zero");
        }

        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new PaymentException(
                    String.format("Payment amount (%.2f) exceeds outstanding amount (%.2f) for this rent",
                            request.getAmount(), outstanding)
            );
        }

        // Check for duplicate reference number (excluding current payment)
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().trim().isEmpty()) {
            Optional<Payment> existing = paymentRepository.findByReferenceNumber(request.getReferenceNumber().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new DuplicatePaymentException("Payment with reference number " + request.getReferenceNumber() + " already exists");
            }
        }

        // Validate payment date
        if (request.getPaymentDate().isAfter(LocalDate.now())) {
            throw new PaymentException("Payment date cannot be in the future");
        }

        // Update payment
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(convertToEntityPaymentMethod(request.getPaymentMethod()));
        payment.setReferenceNumber(request.getReferenceNumber() != null ? request.getReferenceNumber().trim() : null);
        payment.setNotes(request.getNotes());
        payment.setStatus(calculatePaymentStatus(request.getAmount(), outstanding));

        Payment updated = paymentRepository.save(payment);
        log.info("Payment updated with id: {}", updated.getId());

        // Update rent status based on total payments
        updateRentStatusBasedOnPayments(rent);

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePayment(Long id) {
        log.info("Deleting payment with id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        Rent rent = payment.getRent();
        paymentRepository.delete(payment);
        log.info("Payment deleted with id: {}", id);

        // Update rent status based on remaining payments
        updateRentStatusBasedOnPayments(rent);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long id) {
        log.info("Cancelling payment with id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        Rent rent = payment.getRent();
        payment.setStatus(Payment.Status.CANCELLED);
        Payment updated = paymentRepository.save(payment);
        log.info("Payment cancelled with id: {}", updated.getId());

        // Update rent status based on remaining payments
        updateRentStatusBasedOnPayments(rent);

        return mapToResponse(updated);
    }

    private Payment.Status calculatePaymentStatus(BigDecimal paymentAmount, BigDecimal outstandingBeforePayment) {
        BigDecimal remainingAfterPayment = outstandingBeforePayment.subtract(paymentAmount);
        if (remainingAfterPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return Payment.Status.PAID;
        }
        return Payment.Status.PARTIAL;
    }

    private void updateRentStatusBasedOnPayments(Rent rent) {
        BigDecimal totalPaid = getTotalPaidForRent(rent.getId());
        Rent.Status newStatus;

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = Rent.Status.PENDING;
        } else if (totalPaid.compareTo(rent.getMonthlyRent()) >= 0) {
            newStatus = Rent.Status.PAID;
        } else {
            newStatus = Rent.Status.PARTIAL;
        }

        // Only update if status actually changed
        if (rent.getStatus() != newStatus) {
            rent.setStatus(newStatus);
            if (newStatus == Rent.Status.PAID) {
                rent.setPaidDate(LocalDate.now());
                rent.setPaidAmount(rent.getMonthlyRent());
            } else if (newStatus == Rent.Status.PARTIAL) {
                rent.setPaidAmount(totalPaid);
                rent.setPaidDate(LocalDate.now());
            } else {
                rent.setPaidDate(null);
                rent.setPaidAmount(null);
            }
            rentRepository.save(rent);
            log.info("Rent status updated to {} for rent id: {}", newStatus, rent.getId());
        }
    }

    private Payment.PaymentMethod convertToEntityPaymentMethod(PaymentRequest.PaymentMethod dtoMethod) {
        return switch (dtoMethod) {
            case CASH -> Payment.PaymentMethod.CASH;
            case UPI -> Payment.PaymentMethod.UPI;
            case CARD -> Payment.PaymentMethod.CARD;
            case BANK_TRANSFER -> Payment.PaymentMethod.BANK_TRANSFER;
            case CHEQUE -> Payment.PaymentMethod.CHEQUE;
            case OTHER -> Payment.PaymentMethod.OTHER;
        };
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.from(payment);
    }
}