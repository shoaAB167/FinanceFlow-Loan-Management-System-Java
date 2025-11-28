package com.finance.loanms.service.impl;

import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.model.payload.RiskAssessment;
import com.finance.loanms.service.CreditRiskService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MLBasedCreditRiskService implements CreditRiskService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private static final String SERVICE_NAME = "mlService";

    public MLBasedCreditRiskService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "fallbackRiskAssessment")
    public RiskAssessment assessRisk(CreateLoanRequest request) {
        return restTemplate.postForObject(mlServiceUrl, request, RiskAssessment.class);
    }

    public RiskAssessment fallbackRiskAssessment(CreateLoanRequest request, Throwable t) {
        log.error("ML Service unavailable, circuit breaker fallback triggered. Reason: {}", t.getMessage());
        return RiskAssessment.builder()
                .isApproved(false)
                .riskScore(0.0)
                .reason("Risk assessment service unavailable (Fallback)")
                .build();
    }
}
