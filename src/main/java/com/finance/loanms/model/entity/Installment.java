package com.finance.loanms.model.entity;

import com.finance.loanms.model.enumtype.InstallmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int installmentNumber;
    private LocalDate dueDate;
    private double principalComponent;
    private double interestComponent;
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;

    @ManyToOne(optional = false)
    private LoanAccount loanAccount;

    @OneToOne(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Repayment repayment;
}
