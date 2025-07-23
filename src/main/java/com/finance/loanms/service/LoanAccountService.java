package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.dto.request.ForecloseLoanRequest;
import com.finance.loanms.dto.response.LoanResponse;

public interface LoanAccountService {

    ApiResponse<LoanResponse> createLoan(CreateLoanRequest request);

    ApiResponse<LoanResponse> forecloseLoan(Long loanId, ForecloseLoanRequest request);

    ApiResponse<LoanResponse> getLoanById(Long loanId);
}
