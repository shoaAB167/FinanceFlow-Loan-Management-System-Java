package com.finance.loanms.dto.response;

import java.time.LocalDate;
public record RepaymentHistory(
        Long id,
        double amount,
        LocalDate paymentDate,
        String mode,
        String transactionId,
        int installmentNumber
) {}

