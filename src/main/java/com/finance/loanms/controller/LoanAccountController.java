package com.finance.loanms.controller;

import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.dto.request.ForecloseLoanRequest;
import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.LoanResponse;
import com.finance.loanms.service.LoanAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanAccountController {

    private final LoanAccountService loanAccountService;

    public LoanAccountController(LoanAccountService loanAccountService) {
        this.loanAccountService = loanAccountService;
    }

    /**
     * Create Loan API
     * URL: POST /loans
     * Body: CreateLoanRequest
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        return ResponseEntity.ok(loanAccountService.createLoan(request));
    }

    /**
     * Foreclose Loan API
     * URL: POST /loans/{loanId}/foreclose
     * Body: ForecloseLoanRequest
     */
    @PostMapping("/{loanId}/foreclose")
    public ResponseEntity<ApiResponse<LoanResponse>> forecloseLoan(@PathVariable Long loanId,
                                                                   @Valid @RequestBody ForecloseLoanRequest request) {
        return ResponseEntity.ok(loanAccountService.forecloseLoan(loanId, request));
    }

    /**
     * Fetch Loan Details by ID (optional helper)
     * URL: GET /loans/{loanId}
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetails(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanAccountService.getLoanById(loanId));
    }
}
