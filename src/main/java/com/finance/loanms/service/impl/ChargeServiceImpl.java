package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.ChargeRequest;
import com.finance.loanms.dto.response.ChargeResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Charge;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.repository.ChargeRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.service.ChargeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeServiceImpl implements ChargeService {

    private final ChargeRepository chargeRepository;
    private final LoanAccountRepository loanAccountRepository;

    public ChargeServiceImpl(ChargeRepository chargeRepository, LoanAccountRepository loanAccountRepository) {
        this.chargeRepository = chargeRepository;
        this.loanAccountRepository = loanAccountRepository;
    }

    @Override
    @Transactional
    public ApiResponse<ChargeResponse> addCharge(Long loanId, ChargeRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Charge request cannot be null");
            }
            if (request.getAmount() <= 0) {
                throw new IllegalArgumentException("Charge amount must be greater than 0");
            }
            if (request.getType() == null) {
                throw new IllegalArgumentException("Charge type is required");
            }

            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            Charge charge = Charge.builder()
                    .type(request.getType())
                    .amount(request.getAmount())
                    .appliedDate(request.getAppliedDate())
                    .description(request.getDescription())
                    .loanAccount(loanAccount)
                    .build();

            Charge savedCharge = chargeRepository.save(charge);

            ChargeResponse response = ChargeResponse.builder()
                    .chargeId(savedCharge.getId())
                    .type(savedCharge.getType())
                    .amount(savedCharge.getAmount())
                    .appliedDate(savedCharge.getAppliedDate())
                    .description(request.getDescription())
                    .loanAccountId(loanAccount.getId())
                    .build();

            return ApiResponse.ok("Charge added successfully", response);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add charge: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<ChargeResponse.ChargeListResponse> getChargesByLoan(Long loanId) {
        try {
            if (loanId == null || loanId <= 0) {
                throw new IllegalArgumentException("Invalid loan ID");
            }

            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            List<Charge> charges = chargeRepository.findByLoanAccount(loanAccount);

            List<ChargeResponse.ChargeListResponse.ChargeDetails> chargeDetails = charges.stream()
                    .map(charge -> ChargeResponse.ChargeListResponse.ChargeDetails.builder()
                            .chargeId(charge.getId())
                            .type(charge.getType())
                            .amount(charge.getAmount())
                            .appliedDate(charge.getAppliedDate())
                            .description(charge.getDescription() != null ? charge.getDescription() : "")
                            .build())
                    .collect(Collectors.toList());

            double totalCharges = charges.stream()
                    .mapToDouble(Charge::getAmount)
                    .sum();

            ChargeResponse.ChargeListResponse response = ChargeResponse.ChargeListResponse.builder()
                    .loanAccountId(loanId)
                    .charges(chargeDetails)
                    .totalCharges(totalCharges)
                    .build();

            return ApiResponse.ok("Charges retrieved successfully", response);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve charges: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> removeCharge(Long loanId, Long chargeId) {
        try {
            if (loanId == null || loanId <= 0) {
                throw new IllegalArgumentException("Invalid loan ID");
            }
            if (chargeId == null || chargeId <= 0) {
                throw new IllegalArgumentException("Invalid charge ID");
            }

            loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            Charge charge = chargeRepository.findById(chargeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Charge not found with ID: " + chargeId));

            if (!charge.getLoanAccount().getId().equals(loanId)) {
                throw new IllegalStateException("Charge does not belong to the specified loan");
            }

            chargeRepository.delete(charge);

            return ApiResponse.ok("Charge removed successfully", "Charge with ID " + chargeId + " has been removed");
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove charge: " + e.getMessage(), e);
        }
    }
}
