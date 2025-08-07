package com.finance.loanms.controller;

import com.finance.loanms.dto.request.RepaymentRequest;
import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.RepaymentHistoryResponse;
import com.finance.loanms.dto.response.RepaymentResponse;
import com.finance.loanms.service.RepaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans/{loanId}/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepaymentResponse>> applyRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentRequest request) {

        return ResponseEntity.ok(repaymentService.applyRepayment(loanId, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RepaymentHistoryResponse>> getRepaymentHistory(@PathVariable Long loanId) {
        return ResponseEntity.ok(repaymentService.getRepaymentHistory(loanId));
    }
}
