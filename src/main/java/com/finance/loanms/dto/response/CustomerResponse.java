package com.finance.loanms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerResponse {

    private Long id;
    private String customerId;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LoanSummary> loans;

    @Data
    @Builder
    public static class LoanSummary {
        private Long loanId;
        private String loanAccountId;
        private Double principal;
        private String status;
        private Integer tenureMonths;
    }
}
