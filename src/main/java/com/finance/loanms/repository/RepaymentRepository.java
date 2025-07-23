package com.finance.loanms.repository;

import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    boolean existsByTransactionId(String transactionId);

    List<Repayment> findByLoanAccountOrderByPaymentDateAsc(LoanAccount loanAccount);
}
