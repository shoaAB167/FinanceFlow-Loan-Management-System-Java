package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.RepaymentRequest;
import com.finance.loanms.dto.response.RepaymentResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
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

            // 2. Check for existing transactionId (idempotency)
            if (repaymentRepository.existsByTransactionId(request.transactionId())) {
                return ApiResponse.fail("Duplicate transaction ignored");
            }

            double amountToApply = request.amountPaid();

            // 3. Apply payment against due installments (FIFO)
            List<Installment> installments = installmentRepository.findByLoanAccountOrderByInstallmentNumberAsc(loanAccount);

            for (Installment installment : installments) {
                if (installment.getStatus() == InstallmentStatus.PAID) continue;

                double pendingAmount = installment.getTotalAmount();
                if (amountToApply >= pendingAmount) {
                    installment.setStatus(InstallmentStatus.PAID);
                    amountToApply -= pendingAmount;
                } else {
                    // Partial payment handling (optional based on business rule)
                    // Here we just deduct from pending and maybe keep status DUE
                    amountToApply = 0;
                }
                installmentRepository.save(installment);

                if (amountToApply == 0) break;
            }

            // 4. Save Repayment record
            Repayment repayment = Repayment.builder()
                    .loanAccount(loanAccount)
                    .amount(request.amountPaid())
                    .paymentDate(request.paymentDate())
                    .mode(request.mode())
                    .transactionId(request.transactionId())
                    .build();

            repayment = repaymentRepository.save(repayment);

            RepaymentResponse response = new RepaymentResponse("Repayment applied successfully", repayment.getId());
            return ApiResponse.ok("Repayment processed successfully", response);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to process repayment: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<RepaymentResponse> getRepaymentHistory(Long loanId) {
        try {
            // 1. Validate LoanAccount
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElse(null);
            if (loanAccount == null) {
                return ApiResponse.fail("Loan not found");
            }

            // 2. Fetch repayments and map to response
            List<Repayment> repayments = repaymentRepository.findByLoanAccountOrderByPaymentDateAsc(loanAccount);
            
            // TODO: Create proper RepaymentResponse with repayment history
            RepaymentResponse response = new RepaymentResponse("Repayment history retrieved", null);
            return ApiResponse.ok("Repayment history fetched successfully", response);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to fetch repayment history: " + e.getMessage());
        }
    }
}
