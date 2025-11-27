package com.finance.loanms.service.impl;

import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.model.payload.RiskAssessment;
import com.finance.loanms.service.CreditRiskService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
public class MLBasedCreditRiskService implements CreditRiskService {

    private final RestTemplate restTemplate;
    private static final String ML_SERVICE_URL = "http://localhost:5000/predict";

    public MLBasedCreditRiskService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public RiskAssessment assessRisk(CreateLoanRequest request) {
        try {
            return restTemplate.postForObject(ML_SERVICE_URL, request, RiskAssessment.class);
        } catch (Exception e) {
            // Fallback to safe default or throw exception
            // For now, we'll log and return a rejection to be safe if ML service is down
            System.err.println("ML Service unavailable: " + e.getMessage());
            return RiskAssessment.builder()
                    .isApproved(false)
                    .riskScore(0.0)
                    .reason("Risk assessment service unavailable")
                    .build();
        }
    }
}
