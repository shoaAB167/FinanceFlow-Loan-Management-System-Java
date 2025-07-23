package com.finance.loanms.service;

import com.finance.loanms.model.entity.LoanAccount;

public interface ScheduleService {

    void generateSchedule(LoanAccount loanAccount);

    void updateScheduleAfterRateChange(LoanAccount loanAccount, double newRate, int effectiveFromInstallment);
}

