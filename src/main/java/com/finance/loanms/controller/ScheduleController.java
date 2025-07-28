package com.finance.loanms.controller;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.response.ScheduleResponse;
import com.finance.loanms.service.ScheduleService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans/{loanId}/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> getSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(scheduleService.getSchedule(loanId));
    }

    @PutMapping("/rate-change")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateScheduleAfterRateChange(
            @PathVariable Long loanId,
            @RequestParam Double newInterestRate,
            @RequestParam @Min(1) Integer effectiveFromInstallment) {

        return ResponseEntity.ok(scheduleService.updateScheduleAfterRateChange(loanId, newInterestRate, effectiveFromInstallment));
    }
}
