package com.finance.loanms.repository;

import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.model.enumtype.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByEmail(String email);

    boolean existsByCustomerId(String customerId);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name%")
    List<Customer> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM Customer c JOIN c.loanAccounts la WHERE la.status = :status")
    List<Customer> findCustomersWithLoansByStatus(@Param("status") LoanStatus status);

    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanAccounts) > 0")
    List<Customer> findCustomersWithLoans();
}
