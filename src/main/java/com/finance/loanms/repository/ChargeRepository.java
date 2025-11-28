package com.finance.loanms.repository;

import com.finance.loanms.model.entity.Charge;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByLoanAccount(LoanAccount loanAccount);

    List<Charge> findByLoanAccountAndType(LoanAccount loanAccount, ChargeType type);

    List<Charge> findByLoanAccountId(Long loanAccountId);

    List<Charge> findByLoanAccountAndIsPaidFalse(LoanAccount loanAccount);

    boolean existsByLoanAccountIdAndIsPaidFalse(Long loanId);
}
