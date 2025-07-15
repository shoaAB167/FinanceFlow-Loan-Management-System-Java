package com.finance.loanms.model.entity;

import com.finance.loanms.model.enumtype.InterestType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRate {

    @Enumerated(EnumType.STRING)
    private InterestType type;

    private double baseRate;

    @ElementCollection
    @CollectionTable(name = "stepped_rates", joinColumns = @JoinColumn(name = "loan_account_id"))
    @MapKeyColumn(name = "installment_number")
    @Column(name = "rate")
    private Map<Integer, Double> steppedRates = new HashMap<>();
}
