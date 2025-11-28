package com.finance.loanms.model.entity;

import com.finance.loanms.model.enumtype.ChargeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charge extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChargeType type;

    private double amount;
    private LocalDate appliedDate;
    private String description;

    @Builder.Default
    private boolean isPaid = false;

    @ManyToOne(optional = false)
    private LoanAccount loanAccount;
}
