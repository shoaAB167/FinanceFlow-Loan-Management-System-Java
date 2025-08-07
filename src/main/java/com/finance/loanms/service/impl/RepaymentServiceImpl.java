package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.RepaymentRequest;
import com.finance.loanms.dto.response.RepaymentHistory;
import com.finance.loanms.dto.response.RepaymentHistoryResponse;
import com.finance.loanms.dto.response.RepaymentResponse;
import com.finance.loanms.model.entity.Installment;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.entity.Repayment;
import com.finance.loanms.model.enumtype.InstallmentStatus;
import com.finance.loanms.repository.InstallmentRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.repository.RepaymentRepository;
import com.finance.loanms.service.RepaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RepaymentServiceImpl implements RepaymentService {

    private final LoanAccountRepository loanAccountRepository;
    private final InstallmentRepository installmentRepository;
    private final RepaymentRepository repaymentRepository;

    public RepaymentServiceImpl(LoanAccountRepository loanAccountRepository,
                                InstallmentRepository installmentRepository,
                                RepaymentRepository repaymentRepository) {
        this.loanAccountRepository = loanAccountRepository;
        this.installmentRepository = installmentRepository;
        this.repaymentRepository = repaymentRepository;
    }

    @Transactional
    @Override
    public ApiResponse<RepaymentResponse> applyRepayment(Long loanId, RepaymentRequest request) {
        try {
            // 1. Validate LoanAccount
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElse(null);
            if (loanAccount == null) {
                return ApiResponse.fail("Loan not found");
            }

            // 2. Check for duplicate transactionId
            if (repaymentRepository.existsByTransactionId(request.transactionId())) {
                return ApiResponse.fail("Duplicate transaction ID");
            }

            double amountToApply = request.amountPaid();

            // 3. Fetch due installments (FIFO)
            List<Installment> installments = installmentRepository
                    .findByLoanAccountOrderByInstallmentNumberAsc(loanAccount);

            for (Installment installment : installments) {
                if (installment.getStatus() == InstallmentStatus.PAID) continue;

                double totalPaid = installment.getRepayments().stream()
                        .mapToDouble(Repayment::getAmount)
                        .sum();

                double pendingAmount = installment.getTotalAmount() - totalPaid;

                // Skip if amount is not exactly equal to the pending amount
                BigDecimal amountToApplyRounded = BigDecimal.valueOf(amountToApply).setScale(2, RoundingMode.HALF_UP);
                BigDecimal pendingAmountRounded = BigDecimal.valueOf(pendingAmount).setScale(2, RoundingMode.HALF_UP);

                if (amountToApplyRounded.compareTo(pendingAmountRounded) != 0) {
                    return ApiResponse.fail("Amount does not match the next due EMI: ₹" + pendingAmountRounded);
                }

                // Create repayment
                Repayment repayment = Repayment.builder()
                        .loanAccount(loanAccount)
                        .installment(installment)
                        .amount(amountToApply)
                        .paymentDate(request.paymentDate())
                        .transactionId(request.transactionId())
                        .mode(request.mode())
                        .build();

                repayment = repaymentRepository.save(repayment);
                installment.getRepayments().add(repayment);
                installment.setStatus(InstallmentStatus.PAID);

                installmentRepository.save(installment);

                return ApiResponse.ok("Repayment applied successfully",
                        new RepaymentResponse("Repayment processed", null));
            }

            return ApiResponse.fail("No due installment found to match the payment amount");
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply repayment: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<RepaymentHistoryResponse> getRepaymentHistory(Long loanId) {
        try {
            // 1. Validate LoanAccount
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElse(null);
            if (loanAccount == null) {
                return ApiResponse.fail("Loan not found");
            }

            // 2. Fetch repayments sorted by payment date
            List<Repayment> repayments = repaymentRepository
                    .findByLoanAccountOrderByPaymentDateAsc(loanAccount);

            // 3. Map to DTOs
            List<RepaymentHistory> repaymentDtos = repayments.stream()
                    .map(r -> new RepaymentHistory(
                            r.getId(),
                            r.getAmount(),
                            r.getPaymentDate(),
                            r.getMode(), // assuming enum
                            r.getTransactionId(),
                            r.getInstallment().getInstallmentNumber()
                    ))
                    .toList();

            // 4. Build response
            RepaymentHistoryResponse response = new RepaymentHistoryResponse(
                    "Repayment history retrieved",
                    repaymentDtos
            );

            return ApiResponse.ok("Repayment history fetched successfully", response);

        } catch (Exception e) {
            return ApiResponse.fail("Failed to fetch repayment history: " + e.getMessage());
        }
    }
}
