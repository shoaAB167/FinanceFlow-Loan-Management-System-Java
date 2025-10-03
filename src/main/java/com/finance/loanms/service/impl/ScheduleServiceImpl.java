package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.ScheduleResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Installment;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.InstallmentStatus;
import com.finance.loanms.model.enumtype.InterestType;
import com.finance.loanms.repository.InstallmentRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final InstallmentRepository installmentRepository;
    private final LoanAccountRepository loanAccountRepository;

    public ScheduleServiceImpl(InstallmentRepository installmentRepository, LoanAccountRepository loanAccountRepository) {
        this.installmentRepository = installmentRepository;
        this.loanAccountRepository = loanAccountRepository;
    }

    public void generateSchedule(LoanAccount loanAccount) {
        try {
            if (loanAccount == null) {
                throw new IllegalArgumentException("Loan account cannot be null");
            }
            if (loanAccount.getPrincipal() <= 0) {
                throw new IllegalArgumentException("Principal amount must be greater than 0");
            }
            if (loanAccount.getTenureMonths() <= 0) {
                throw new IllegalArgumentException("Tenure must be greater than 0");
            }

            List<Installment> installments = new ArrayList<>();
            double principal = loanAccount.getPrincipal();
            int tenure = loanAccount.getTenureMonths();
            double principalComponent = principal / tenure;

            Map<Integer, Double> steppedRates = loanAccount.getInterestRate().getSteppedRates();
            InterestType type = loanAccount.getInterestRate().getType();
            LocalDate startDate = loanAccount.getStartDate();

            if (type == InterestType.STEP && (steppedRates == null || steppedRates.isEmpty())) {
                throw new IllegalStateException("Stepped rates are required for STEP interest type");
            }

            for (int i = 1; i <= tenure; i++) {
                double annualRate;

                if (type == InterestType.STEP) {
                    annualRate = steppedRates.get(i);
                } else {
                    annualRate = loanAccount.getInterestRate().getBaseRate();
                }

                double monthlyRate = annualRate / 12 / 100;
                double interestComponent = principal * monthlyRate;
                double totalAmount = principalComponent + interestComponent;

                Installment installment = Installment.builder()
                        .loanAccount(loanAccount)
                        .installmentNumber(i)
                        .dueDate(startDate.plusMonths(i))
                        .principalComponent(principalComponent)
                        .interestComponent(interestComponent)
                        .totalAmount(totalAmount)
                        .status(InstallmentStatus.DUE)
                        .build();

                installments.add(installment);
            }
            installmentRepository.saveAll(installments);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schedule: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<ScheduleResponse> getSchedule(Long loanId) {
        try {
            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            List<Installment> installments = installmentRepository.findByLoanAccountOrderByInstallmentNumber(loanAccount);

            List<ScheduleResponse.InstallmentEntry> scheduleEntries = installments.stream()
                    .map(installment -> new ScheduleResponse.InstallmentEntry(
                            installment.getInstallmentNumber(),
                            installment.getDueDate(),
                            installment.getPrincipalComponent(),
                            installment.getInterestComponent(),
                            installment.getTotalAmount(),
                            installment.getStatus().toString()
                    ))
                    .collect(Collectors.toList());

            ScheduleResponse response = new ScheduleResponse(loanId, scheduleEntries);
            return ApiResponse.ok("Schedule retrieved successfully", response);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedule: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public ApiResponse<ScheduleResponse> updateScheduleAfterRateChange(Long loanId, double newRate, int effectiveFromInstallment) {
        try {
            if (newRate < 0) {
                throw new IllegalArgumentException("Interest rate cannot be negative");
            }
            if (effectiveFromInstallment < 1) {
                throw new IllegalArgumentException("Effective installment number must be greater than 0");
            }

            LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

            List<Installment> installmentsToUpdate = installmentRepository
                    .findByLoanAccountAndInstallmentNumberGreaterThanEqual(loanAccount, effectiveFromInstallment);

            if (installmentsToUpdate.isEmpty()) {
                throw new ResourceNotFoundException("No installments found from installment number: " + effectiveFromInstallment);
            }

            double newMonthlyInterestRate = newRate / 12 / 100;
            
            for (Installment installment : installmentsToUpdate) {
                double newInterestComponent = loanAccount.getPrincipal() * newMonthlyInterestRate;
                double newTotalAmount = installment.getPrincipalComponent() + newInterestComponent;
                
                installment.setInterestComponent(newInterestComponent);
                installment.setTotalAmount(newTotalAmount);
            }

            installmentRepository.saveAll(installmentsToUpdate);
            loanAccount.getInterestRate().setBaseRate(newRate);
            loanAccountRepository.save(loanAccount);

            return getSchedule(loanId);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update schedule: " + e.getMessage(), e);
        }
    }

    // Helper method for updating schedule after rate change (original signature maintained)
    @Transactional
    public void updateScheduleAfterRateChange(LoanAccount loanAccount, double newRate, int effectiveFromInstallment) {
        updateScheduleAfterRateChange(loanAccount.getId(), newRate, effectiveFromInstallment);
    }
}
