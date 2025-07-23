package com.finance.loanms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RepaymentRequest(
        @NotNull @Min(1)
        Double amountPaid,

        @NotNull
        LocalDate paymentDate,

        @NotBlank
        String mode, // UPI, CASH, ONLINE, etc.

        @NotBlank
        String transactionId
) {}
