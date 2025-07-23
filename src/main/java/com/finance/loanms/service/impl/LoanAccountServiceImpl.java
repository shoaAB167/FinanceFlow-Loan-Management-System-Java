package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CreateLoanRequest;
import com.finance.loanms.dto.request.ForecloseLoanRequest;
import com.finance.loanms.dto.response.LoanResponse;
import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.model.entity.InterestRate;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.LoanStatus;
import com.finance.loanms.repository.CustomerRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.service.LoanAccountService;
import com.finance.loanms.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                    .orElse(null);
            if (customer == null) {
                return ApiResponse.fail("Customer not found");
            }

            // 2. Create InterestRate based on request
            InterestRate interestRate = InterestRate.builder()
                    .type(request.getInterestType())
                    .baseRate(request.getInterestRate())
                    .build();

            // 3. Create LoanAccount entity with principal, tenure, rate
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

            // 4. Generate Installment Schedule using ScheduleService
            scheduleService.generateSchedule(loanAccount);

            // 5. Map to LoanResponse DTO and return
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
}
