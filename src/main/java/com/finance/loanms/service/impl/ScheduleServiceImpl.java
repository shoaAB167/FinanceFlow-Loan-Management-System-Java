package com.finance.loanms.service.impl;

import com.finance.loanms.model.entity.Installment;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.InstallmentStatus;
import com.finance.loanms.repository.InstallmentRepository;
import com.finance.loanms.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final InstallmentRepository installmentRepository;

    public ScheduleServiceImpl(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
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

    @Transactional
    @Override
    public void updateScheduleAfterRateChange(LoanAccount loanAccount, double newRate, int effectiveFromInstallment) {
        // TODO: Fetch installments >= effectiveFromInstallment and recalculate
        // TODO: Update interestComponent and totalAmount with newRate
        // Save updated installments
    }
}
