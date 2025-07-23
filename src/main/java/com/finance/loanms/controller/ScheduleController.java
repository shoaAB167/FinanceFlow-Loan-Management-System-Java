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
    public ResponseEntity<ApiResponse<String>> getSchedule(@PathVariable Long loanId) {
        // TODO: Implement proper schedule fetching when ScheduleResponse is available
        return ResponseEntity.ok(ApiResponse.ok("Schedule fetched successfully", "Schedule feature not implemented yet"));
    }

    @PutMapping("/rate-change")
    public ResponseEntity<ApiResponse<String>> updateScheduleAfterRateChange(
            @PathVariable Long loanId,
            @RequestParam Double newInterestRate,
            @RequestParam @Min(1) Integer effectiveFromInstallment) {

        // TODO: Implement proper schedule update when ScheduleResponse is available
        return ResponseEntity.ok(ApiResponse.ok("Schedule updated successfully", "Schedule update feature not implemented yet"));
    }
}
