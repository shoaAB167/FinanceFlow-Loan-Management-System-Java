package com.finance.loanms.repository;

import com.finance.loanms.model.entity.Installment;
import com.finance.loanms.model.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByLoanAccountOrderByInstallmentNumberAsc(LoanAccount loanAccount);

    List<Installment> findByLoanAccountAndInstallmentNumberGreaterThanEqualOrderByInstallmentNumberAsc(LoanAccount loanAccount, int installmentNumber);
}

