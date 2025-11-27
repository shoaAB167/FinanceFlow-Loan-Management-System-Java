package com.finance.loanms.service;

import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.model.payload.RiskAssessment;

public interface CreditRiskService {
    RiskAssessment assessRisk(CreateLoanRequest request);
}
