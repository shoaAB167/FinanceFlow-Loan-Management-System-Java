package com.finance.loanms.repository;

import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.model.entity.LoanAccount;
import com.finance.loanms.model.enumtype.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    Optional<LoanAccount> findByLoanId(String loanId);

    Optional<LoanAccount> findByIdAndStatus(Long id, LoanStatus status);

    List<LoanAccount> findByCustomer(Customer customer);

    List<LoanAccount> findByCustomerAndStatus(Customer customer, LoanStatus status);

    List<LoanAccount> findByStatus(LoanStatus status);

    List<LoanAccount> findByCustomerOrderByStartDateDesc(Customer customer);

    boolean existsByLoanId(String loanId);

    @Query("SELECT la FROM LoanAccount la WHERE la.customer.customerId = :customerId")
    List<LoanAccount> findByCustomerCustomerId(@Param("customerId") String customerId);

    @Query("SELECT la FROM LoanAccount la WHERE la.customer.customerId = :customerId AND la.status = :status")
    List<LoanAccount> findByCustomerCustomerIdAndStatus(@Param("customerId") String customerId, @Param("status") LoanStatus status);

    @Query("SELECT la FROM LoanAccount la WHERE la.startDate BETWEEN :startDate AND :endDate")
    List<LoanAccount> findByStartDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT la FROM LoanAccount la WHERE la.principal >= :minAmount AND la.principal <= :maxAmount")
    List<LoanAccount> findByPrincipalBetween(@Param("minAmount") double minAmount, @Param("maxAmount") double maxAmount);

    @Query("SELECT la FROM LoanAccount la WHERE la.tenureMonths = :tenureMonths")
    List<LoanAccount> findByTenureMonths(@Param("tenureMonths") int tenureMonths);

    @Query("SELECT COUNT(la) FROM LoanAccount la WHERE la.customer = :customer AND la.status = :status")
    long countByCustomerAndStatus(@Param("customer") Customer customer, @Param("status") LoanStatus status);

    @Query("SELECT SUM(la.principal) FROM LoanAccount la WHERE la.customer = :customer AND la.status = :status")
    Double sumPrincipalByCustomerAndStatus(@Param("customer") Customer customer, @Param("status") LoanStatus status);
}
