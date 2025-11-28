# Circuit Breaker Pattern Implementation

## Overview

The Circuit Breaker pattern is a design pattern used in modern software development to detect failures and encapsulate the logic of preventing a failure from constantly recurring, during maintenance, temporary external system failure or unexpected system difficulties.

In this project, we use **Resilience4j** to implement the Circuit Breaker pattern for the integration between the Java Loan Management System and the Python ML Credit Risk Service.

## Why use it here?

The ML Service is an external dependency accessed via HTTP. If this service goes down or becomes slow:

1.  **Cascading Failures**: Threads in the Java application would be blocked waiting for responses, potentially exhausting resources and crashing the entire application.
2.  **Poor User Experience**: Users would wait for a long timeout before getting an error.

The Circuit Breaker prevents this by "opening the circuit" when failures reach a certain threshold, immediately returning a fallback response without calling the failing service.

## Configuration

The configuration is defined in `application.properties`:

```properties
# Enable Circuit Breaker for 'mlService'
resilience4j.circuitbreaker.instances.mlService.registerHealthIndicator=true

# Open circuit if 50% of requests fail
resilience4j.circuitbreaker.instances.mlService.failureRateThreshold=50

# Minimum calls required to calculate failure rate
resilience4j.circuitbreaker.instances.mlService.minimumNumberOfCalls=5

# Wait 5 seconds before trying again (Half-Open state)
resilience4j.circuitbreaker.instances.mlService.waitDurationInOpenState=5s

# Allow 3 calls in Half-Open state to test if service recovered
resilience4j.circuitbreaker.instances.mlService.permittedNumberOfCallsInHalfOpenState=3

# Look at the last 10 calls to calculate failure rate
resilience4j.circuitbreaker.instances.mlService.slidingWindowSize=10
```

## States

1.  **CLOSED**: Normal operation. Requests are passed to the ML Service.
2.  **OPEN**: Failure threshold reached (e.g., ML Service is down). Requests are blocked immediately, and the `fallbackRiskAssessment` method is called.
3.  **HALF-OPEN**: After `waitDurationInOpenState` (5s), a few requests are allowed through. If they succeed, the circuit closes. If they fail, it opens again.

## Implementation Details

The `MLBasedCreditRiskService` class is annotated with `@CircuitBreaker`:

```java
@CircuitBreaker(name = "mlService", fallbackMethod = "fallbackRiskAssessment")
public RiskAssessment assessRisk(CreateLoanRequest request) {
    // Call ML Service
}
```

### Fallback Mechanism

When the circuit is OPEN or an exception occurs, the `fallbackRiskAssessment` method is executed. In our case, it safely rejects the loan application with a specific reason, ensuring the system remains stable and responsive.

```java
public RiskAssessment fallbackRiskAssessment(CreateLoanRequest request, Throwable t) {
    return RiskAssessment.builder()
            .isApproved(false)
            .reason("Risk assessment service unavailable (Fallback)")
            .build();
}
```
