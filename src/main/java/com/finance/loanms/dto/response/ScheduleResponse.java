package com.finance.loanms.dto.response;

import java.time.LocalDate;
import java.util.List;

public class ScheduleResponse {

    private Long loanId;
    private List<InstallmentEntry> schedule;

    public ScheduleResponse(Long loanId, List<InstallmentEntry> schedule) {
        this.loanId = loanId;
        this.schedule = schedule;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public List<InstallmentEntry> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<InstallmentEntry> schedule) {
        this.schedule = schedule;
    }

    public static class InstallmentEntry {
        private int installmentNumber;
        private LocalDate dueDate;
        private double principalComponent;
        private double interestComponent;
        private double totalAmount;
        private String status; // DUE, PAID, LATE, etc.

        public InstallmentEntry(int installmentNumber, LocalDate dueDate, double principalComponent, double interestComponent, double totalAmount, String status) {
            this.installmentNumber = installmentNumber;
            this.dueDate = dueDate;
            this.principalComponent = principalComponent;
            this.interestComponent = interestComponent;
            this.totalAmount = totalAmount;
            this.status = status;
        }

        public int getInstallmentNumber() {
            return installmentNumber;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public double getPrincipalComponent() {
            return principalComponent;
        }

        public double getInterestComponent() {
            return interestComponent;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getStatus() {
            return status;
        }
    }
}
