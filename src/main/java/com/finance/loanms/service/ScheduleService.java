package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.ScheduleResponse;
import com.finance.loanms.model.entity.LoanAccount;

public interface ScheduleService {

    void generateSchedule(LoanAccount loanAccount);

    ApiResponse<ScheduleResponse> getSchedule(Long loanId);

    ApiResponse<ScheduleResponse> updateScheduleAfterRateChange(Long loanId, double newRate, int effectiveFromInstallment);
}

