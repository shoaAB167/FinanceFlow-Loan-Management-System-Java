package com.finance.loanms.model.entity;

import com.finance.loanms.model.enumtype.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanId;

    @ManyToOne(optional = false)
    private Customer customer;

    @Column(nullable = false)
    private double principal;

    @Embedded
    private InterestRate interestRate;

    @Column(nullable = false)
    private int tenureMonths;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> schedule = new ArrayList<>();

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charge> charges = new ArrayList<>();
    
    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repayment> repayments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;
}
