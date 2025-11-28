package com.finance.loanms.exception;

import com.finance.loanms.model.payload.RiskAssessment;
import lombok.Getter;

@Getter
public class LoanRejectionException extends RuntimeException {
    private final RiskAssessment riskAssessment;

    public LoanRejectionException(RiskAssessment riskAssessment) {
        super("Loan rejected: " + riskAssessment.getReason());
        this.riskAssessment = riskAssessment;
    }
}
