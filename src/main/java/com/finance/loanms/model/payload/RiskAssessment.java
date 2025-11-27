package com.finance.loanms.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    private boolean isApproved;
    private double riskScore;
    private String reason;
}
