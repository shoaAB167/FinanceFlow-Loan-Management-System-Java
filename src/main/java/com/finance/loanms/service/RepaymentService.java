package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.RepaymentRequest;
import com.finance.loanms.dto.response.RepaymentHistoryResponse;
import com.finance.loanms.dto.response.RepaymentResponse;

public interface RepaymentService {

    ApiResponse<RepaymentResponse> applyRepayment(Long loanId, RepaymentRequest request);

    ApiResponse<RepaymentHistoryResponse> getRepaymentHistory(Long loanId);
}
