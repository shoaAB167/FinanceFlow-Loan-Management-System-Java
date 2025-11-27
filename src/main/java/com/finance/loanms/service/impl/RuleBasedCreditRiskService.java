package com.finance.loanms.service.impl;

import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.model.payload.RiskAssessment;
import com.finance.loanms.service.CreditRiskService;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedCreditRiskService implements CreditRiskService {

    private static final int MIN_CREDIT_SCORE = 600;
    private static final double MAX_DEBT_TO_INCOME_RATIO = 0.4;

    @Override
    public RiskAssessment assessRisk(CreateLoanRequest request) {
        if (request.getCreditScore() < MIN_CREDIT_SCORE) {
            return RiskAssessment.builder()
                    .isApproved(false)
                    .riskScore(0.0)
                    .reason("Credit score is below minimum requirement of " + MIN_CREDIT_SCORE)
                    .build();
        }

        double monthlyInstallment = request.getPrincipal() / request.getTenureMonths();
        // Simple interest calculation approximation for risk assessment
        if (request.getInterestRate() != null) {
            monthlyInstallment += (request.getPrincipal() * (request.getInterestRate() / 100)) / 12;
        }

        double debtToIncomeRatio = monthlyInstallment / request.getMonthlyIncome();

        if (debtToIncomeRatio > MAX_DEBT_TO_INCOME_RATIO) {
            return RiskAssessment.builder()
                    .isApproved(false)
                    .riskScore(debtToIncomeRatio)
                    .reason("Debt-to-Income ratio " + String.format("%.2f", debtToIncomeRatio) + " exceeds maximum of "
                            + MAX_DEBT_TO_INCOME_RATIO)
                    .build();
        }

        return RiskAssessment.builder()
                .isApproved(true)
                .riskScore(1.0 - debtToIncomeRatio) // Higher score is better
                .reason("Approved based on credit score and income analysis")
                .build();
    }
}
