package com.finance.loanms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ForecloseLoanRequest {

    @NotNull(message = "Foreclosure date is required")
    private LocalDate foreclosureDate;
}

