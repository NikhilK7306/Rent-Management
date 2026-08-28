package com.rentms.dto.payment;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Rent ID is required")
    private Long rentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    public enum PaymentMethod {
        CASH,
        UPI,
        CARD,
        BANK_TRANSFER,
        CHEQUE,
        OTHER
    }
}