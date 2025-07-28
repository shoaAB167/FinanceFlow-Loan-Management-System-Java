package com.finance.loanms.dto.request;

import com.finance.loanms.model.enumtype.ChargeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ChargeRequest {

    @NotNull(message = "Charge type is required")
    private ChargeType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Applied date is required")
    private LocalDate appliedDate;

    private String description;
}
