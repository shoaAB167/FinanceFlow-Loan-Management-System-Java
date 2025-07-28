package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.ChargeRequest;
import com.finance.loanms.dto.response.ChargeResponse;

public interface ChargeService {

    ApiResponse<ChargeResponse> addCharge(Long loanId, ChargeRequest request);

    ApiResponse<ChargeResponse.ChargeListResponse> getChargesByLoan(Long loanId);

    ApiResponse<String> removeCharge(Long loanId, Long chargeId);
}
