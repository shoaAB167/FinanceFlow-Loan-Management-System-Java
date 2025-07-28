package com.finance.loanms.controller;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.ChargeRequest;
import com.finance.loanms.dto.response.ChargeResponse;
import com.finance.loanms.service.ChargeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans/{loanId}/charges")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    /**
     * Add Charge to Loan API
     * URL: POST /loans/{loanId}/charges
     * Body: ChargeRequest
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChargeResponse>> addCharge(
            @PathVariable Long loanId,
            @Valid @RequestBody ChargeRequest request) {
        return ResponseEntity.ok(chargeService.addCharge(loanId, request));
    }

    /**
     * Get All Charges for a Loan API
     * URL: GET /loans/{loanId}/charges
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ChargeResponse.ChargeListResponse>> getChargesByLoan(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(chargeService.getChargesByLoan(loanId));
    }

    /**
     * Remove Charge from Loan API
     * URL: DELETE /loans/{loanId}/charges/{chargeId}
     */
    @DeleteMapping("/{chargeId}")
    public ResponseEntity<ApiResponse<String>> removeCharge(
            @PathVariable Long loanId,
            @PathVariable Long chargeId) {
        return ResponseEntity.ok(chargeService.removeCharge(loanId, chargeId));
    }
}
