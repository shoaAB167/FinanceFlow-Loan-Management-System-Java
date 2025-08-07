package com.finance.loanms.dto.response;

import java.util.List;

public record RepaymentHistoryResponse(
        String message,
        List<RepaymentHistory> repayments
) {}




