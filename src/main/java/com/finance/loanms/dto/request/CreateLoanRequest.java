package com.finance.loanms.dto.request;

import com.finance.loanms.model.enumtype.InterestType;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class CreateLoanRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be positive")
    private Double principal;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private Double interestRate;

    @NotNull(message = "Interest type is required")
    private InterestType interestType;

    @NotNull(message = "Tenure is required")
    @Positive(message = "Tenure must be a positive integer")
    private Integer tenureMonths;
}
