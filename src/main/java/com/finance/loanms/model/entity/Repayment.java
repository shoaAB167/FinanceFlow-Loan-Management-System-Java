package com.finance.loanms.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repayment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private LocalDate paymentDate;
    private String mode; // UPI, ONLINE, CASH
    
    @Column(unique = true)
    private String transactionId;
    
    @ManyToOne(optional = false)
    private LoanAccount loanAccount;

    @OneToOne
    private Installment installment;
}
