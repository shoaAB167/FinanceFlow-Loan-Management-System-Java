package com.finance.loanms.dto.response;

import com.finance.loanms.model.enumtype.ChargeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ChargeResponse {

    private Long chargeId;
    private ChargeType type;
    private Double amount;
    private LocalDate appliedDate;
    private String description;
    private Long loanAccountId;

    @Data
    @Builder
    public static class ChargeListResponse {
        private Long loanAccountId;
        private List<ChargeDetails> charges;
        private Double totalCharges;

        @Data
        @Builder
        public static class ChargeDetails {
            private Long chargeId;
            private ChargeType type;
            private Double amount;
            private LocalDate appliedDate;
            private String description;
        }
    }
}
