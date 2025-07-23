package com.finance.loanms.dto.response;

import com.finance.loanms.model.entity.LoanAccount;

import java.time.LocalDate;

public record LoanResponse(
        Long loanId,
        Long customerId,
        double principal,
        int tenureMonths,
        double interestRate,
        String interestType,
        String status,
        LocalDate startDate
) {
    public static LoanResponse fromEntity(LoanAccount loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getCustomer().getId(),
                loan.getPrincipal(),
                loan.getTenureMonths(),
                loan.getInterestRate().getBaseRate(),
                loan.getInterestRate().getType().name(),
                loan.getStatus().name(),
                loan.getStartDate()
        );
    }
}
