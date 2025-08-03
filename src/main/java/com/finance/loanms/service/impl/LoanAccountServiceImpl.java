package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.dto.request.ForecloseLoanRequest;
import com.finance.loanms.dto.response.LoanResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.model.entity.InterestRate;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.InterestType;
import com.finance.loanms.model.enumtype.LoanStatus;
import com.finance.loanms.repository.CustomerRepository;
import com.finance.loanms.repository.LoanAccountRepository;
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

    public LoanAccountServiceImpl(CustomerRepository customerRepository,
                                  LoanAccountRepository loanAccountRepository,
                                  ScheduleService scheduleService) {
        this.customerRepository = customerRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.scheduleService = scheduleService;
    }

    @Transactional
    @Override
    public ApiResponse<LoanResponse> createLoan(CreateLoanRequest request) {
        try {
            // 1. Validate customer
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

            if (request.getInterestType() != InterestType.STEP && request.getInterestRate() == null) {
                throw new IllegalArgumentException("Interest rate is required for FIXED and FLOATING types");
            }

            // 2. Prepare InterestRate object based on type
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
                Map<Integer, Double> normalizedMap = normalizeSteppedRates(request.getSteppedRates(), request.getTenureMonths());
                interestRate.setSteppedRates(normalizedMap);
            }

            // 3. Create and persist LoanAccount
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

            // 4. Generate schedule
            scheduleService.generateSchedule(loanAccount);

            // 5. Return mapped response
            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan created successfully", response);

        } catch (Exception e) {
            return ApiResponse.fail("Failed to create loan: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ApiResponse<LoanResponse> forecloseLoan(Long loanId, ForecloseLoanRequest request) {
        try {
            // 1. Find active loan
            LoanAccount loanAccount = loanAccountRepository.findByIdAndStatus(loanId, LoanStatus.ACTIVE)
                    .orElse(null);
            if (loanAccount == null) {
                return ApiResponse.fail("Active loan not found");
            }

            // 2. Validate foreclosure eligibility (all dues cleared, etc.)
            // TODO: Implement foreclosure eligibility check in separate method

            // 3. Apply foreclosure charges
            // TODO: Use ChargeService to apply foreclosure charge

            // 4. Mark loan as FORECLOSED and save
            loanAccount.setStatus(LoanStatus.FORECLOSED);
            loanAccount = loanAccountRepository.save(loanAccount);

            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan foreclosed successfully", response);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to foreclose loan: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<LoanResponse> getLoanById(Long loanId) {
        try {
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElse(null);
            if (loanAccount == null) {
                return ApiResponse.fail("Loan not found");
            }

            LoanResponse response = LoanResponse.fromEntity(loanAccount);
            return ApiResponse.ok("Loan details retrieved successfully", response);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to retrieve loan: " + e.getMessage());
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
