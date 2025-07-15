package com.finance.loanms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinanceFlowLoanManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceFlowLoanManagementSystemApplication.class, args);
	}

}
