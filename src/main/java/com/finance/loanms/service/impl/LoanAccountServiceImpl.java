package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.dto.request.ForecloseLoanRequest;
import com.finance.loanms.dto.response.LoanResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.model.entity.InterestRate;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.InstallmentStatus;
import com.finance.loanms.model.enumtype.InterestType;
import com.finance.loanms.model.enumtype.LoanStatus;
import com.finance.loanms.repository.ChargeRepository;
import com.finance.loanms.repository.CustomerRepository;
import com.finance.loanms.repository.InstallmentRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.service.CreditRiskService;
import com.finance.loanms.service.LoanAccountService;
import com.finance.loanms.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LoanAccountServiceImpl implements LoanAccountService {

    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ScheduleService scheduleService;
    private final InstallmentRepository installmentRepository;
    private final ChargeRepository chargeRepository;
    private final CreditRiskService creditRiskService;

    public LoanAccountServiceImpl(CustomerRepository customerRepository,
            LoanAccountRepository loanAccountRepository,
            ScheduleService scheduleService, InstallmentRepository installmentRepository,
            ChargeRepository chargeRepository, CreditRiskService creditRiskService) {
        this.customerRepository = customerRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.scheduleService = scheduleService;
        this.installmentRepository = installmentRepository;
        this.chargeRepository = chargeRepository;
        this.creditRiskService = creditRiskService;
    }

    @Transactional
    @Override
    public ApiResponse<LoanResponse> createLoan(CreateLoanRequest request) {
        try {
            // 1. Validate customer
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with ID: " + request.getCustomerId()));

            // 2. Assess Credit Risk
            var riskAssessment = creditRiskService.assessRisk(request);
            if (!riskAssessment.isApproved()) {
                throw new IllegalStateException("Loan rejected: " + riskAssessment.getReason());
            }

            if ((request.getInterestType() == InterestType.FIXED ||
                    request.getInterestType() == InterestType.FLOATING)
                    && request.getInterestRate() == null) {
                throw new IllegalArgumentException(
                        "Interest rate is required for " + request.getInterestType() + " interest type");
            }

            // 3. Prepare InterestRate object based on type
            InterestRate interestRate = InterestRate.builder()
                    .type(request.getInterestType())
                    .baseRate(request.getInterestRate() != null ? request.getInterestRate() : 0.0)
                    .steppedRates(request.getSteppedRates() != null ? request.getSteppedRates() : new HashMap<>())
                    .build();

            // STEP: Normalize steppedRates if InterestType is STEP
            if (request.getInterestType() == InterestType.STEP) {
                if (request.getSteppedRates() == null || request.getSteppedRates().isEmpty()) {
                    return ApiResponse.fail("Stepped rates are required for STEP interest type");
                }
                Map<Integer, Double> normalizedMap = normalizeSteppedRates(request.getSteppedRates(),
                        request.getTenureMonths());
                interestRate.setSteppedRates(normalizedMap);
            }

            // 4. Create and persist LoanAccount
            LoanAccount loanAccount = LoanAccount.builder()
                    .loanId(UUID.randomUUID().toString())
                    .customer(customer)
                    .principal(request.getPrincipal())
                    .interestRate(interestRate)
                    .tenureMonths(request.getTenureMonths())
                    .startDate(LocalDate.now())
                    .status(LoanStatus.ACTIVE)
                    .build();

            loanAccount = loanAccountRepository.save(loanAccount);

            // 5. Generate schedule
            scheduleService.generateSchedule(loanAccount);

            // 6. Return mapped response
            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan created successfully", response);

        } catch (ResourceNotFoundException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public ApiResponse<LoanResponse> forecloseLoan(Long loanId, ForecloseLoanRequest request) {
        try {
            // 1. Find active loan
            LoanAccount loanAccount = loanAccountRepository.findByIdAndStatus(loanId, LoanStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("Active loan not found with ID: " + loanId));

            // 2. Check if any unpaid installments exist
            boolean hasDueInstallments = installmentRepository.existsByLoanAccountIdAndStatus(loanId,
                    InstallmentStatus.DUE);
            if (hasDueInstallments) {
                throw new IllegalStateException("Loan cannot be foreclosed — unpaid installments exist");
            }

            // 3. Check if any charges exist
            boolean hasCharges = chargeRepository.existsById(loanId);
            if (hasCharges) {
                throw new IllegalStateException("Loan cannot be foreclosed — outstanding charges exist");
            }

            // 4. Mark loan as FORECLOSED and save
            loanAccount.setStatus(LoanStatus.FORECLOSED);
            loanAccount = loanAccountRepository.save(loanAccount);

            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan foreclosed successfully", response);

        } catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to foreclose loan: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<LoanResponse> getLoanById(Long loanId) {
        try {
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan details retrieved successfully", response);
        } catch (ResourceNotFoundException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve loan: " + e.getMessage(), e);
        }
    }

    private Map<Integer, Double> normalizeSteppedRates(Map<Integer, Double> inputSteps, int tenureMonths) {
        Map<Integer, Double> expanded = new HashMap<>();
        var sortedKeys = inputSteps.keySet().stream().sorted().toList();

        for (int i = 1; i <= tenureMonths; i++) {
            double currentRate = 0;
            for (int j = sortedKeys.size() - 1; j >= 0; j--) {
                if (i >= sortedKeys.get(j)) {
                    currentRate = inputSteps.get(sortedKeys.get(j));
                    break;
                }
            }
            expanded.put(i, currentRate);
        }

        return expanded;
    }

}
