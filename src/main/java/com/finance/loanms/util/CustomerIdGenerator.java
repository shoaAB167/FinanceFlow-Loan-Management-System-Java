package com.finance.loanms.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CustomerIdGenerator {

    private static final AtomicLong counter = new AtomicLong(1);
    private static final String PREFIX = "CUST";
    
    /**
     * Generates a unique customer ID in format: CUST-YYYYMMDD-XXXXX
     * Example: CUST-20240127-00001
     */
    public String generateCustomerId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String counterStr = String.format("%05d", counter.getAndIncrement());
        return String.format("%s-%s-%s", PREFIX, dateStr, counterStr);
    }
    
    /**
     * Alternative simple format: CUSTXXXXX
     * Example: CUST00001
     */
    public String generateSimpleCustomerId() {
        return String.format("%s%05d", PREFIX, counter.getAndIncrement());
    }
}
