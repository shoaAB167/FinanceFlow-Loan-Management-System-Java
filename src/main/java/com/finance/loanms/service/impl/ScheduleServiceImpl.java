package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.ScheduleResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Installment;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.InstallmentStatus;
import com.finance.loanms.repository.InstallmentRepository;
import com.finance.loanms.repository.LoanAccountRepository;
import com.finance.loanms.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final InstallmentRepository installmentRepository;
    private final LoanAccountRepository loanAccountRepository;

    public ScheduleServiceImpl(InstallmentRepository installmentRepository, LoanAccountRepository loanAccountRepository) {
        this.installmentRepository = installmentRepository;
        this.loanAccountRepository = loanAccountRepository;
    }

    @Transactional
    @Override
    public void generateSchedule(LoanAccount loanAccount) {
        List<Installment> installments = new ArrayList<>();

        double principalComponent = loanAccount.getPrincipal() / loanAccount.getTenureMonths();
        double monthlyInterestRate = loanAccount.getInterestRate().getBaseRate() / 12 / 100;

        LocalDate dueDate = loanAccount.getStartDate();

        for (int i = 1; i <= loanAccount.getTenureMonths(); i++) {
            double interestComponent = loanAccount.getPrincipal() * monthlyInterestRate;
            double totalAmount = principalComponent + interestComponent;

            Installment installment = Installment.builder()
                    .loanAccount(loanAccount)
                    .installmentNumber(i)
                    .dueDate(dueDate.plusMonths(i))
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .totalAmount(totalAmount)
                    .status(InstallmentStatus.DUE)
                    .build();

            installments.add(installment);
        }

        installmentRepository.saveAll(installments);
    }

    @Override
    public ApiResponse<ScheduleResponse> getSchedule(Long loanId) {
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
    }

    @Transactional
    @Override
    public ApiResponse<ScheduleResponse> updateScheduleAfterRateChange(Long loanId, double newRate, int effectiveFromInstallment) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + loanId));

        // Fetch installments from the effective installment number onwards
        List<Installment> installmentsToUpdate = installmentRepository
                .findByLoanAccountAndInstallmentNumberGreaterThanEqual(loanAccount, effectiveFromInstallment);

        if (installmentsToUpdate.isEmpty()) {
            throw new ResourceNotFoundException("No installments found from installment number: " + effectiveFromInstallment);
        }

        // Calculate new monthly interest rate
        double newMonthlyInterestRate = newRate / 12 / 100;
        
        // Update each installment with new interest rate
        for (Installment installment : installmentsToUpdate) {
            double newInterestComponent = loanAccount.getPrincipal() * newMonthlyInterestRate;
            double newTotalAmount = installment.getPrincipalComponent() + newInterestComponent;
            
            installment.setInterestComponent(newInterestComponent);
            installment.setTotalAmount(newTotalAmount);
        }

        // Save updated installments
        installmentRepository.saveAll(installmentsToUpdate);

        // Update the loan account's interest rate
        loanAccount.getInterestRate().setBaseRate(newRate);
        loanAccountRepository.save(loanAccount);

        // Return updated schedule
        return getSchedule(loanId);
    }

    // Helper method for updating schedule after rate change (original signature maintained)
    @Transactional
    public void updateScheduleAfterRateChange(LoanAccount loanAccount, double newRate, int effectiveFromInstallment) {
        updateScheduleAfterRateChange(loanAccount.getId(), newRate, effectiveFromInstallment);
    }
}
