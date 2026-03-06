# FinanceFlow — Loan Management System

A Spring Boot backend for managing loan accounts, customers, repayments, and schedules. Includes a Python/Flask ML microservice for credit risk assessment.

## Tech Stack

- **Java 21** + Spring Boot 3.5.3
- **Spring Security** + JWT (access & refresh tokens)
- **MySQL 8** + JPA/Hibernate
- **Redis** (caching)
- **Resilience4j** (circuit breaker)
- **Maven** + Lombok
- **ML Service**: Python/Flask + Random Forest (`ml-service/`)

## Project Structure

```
├── src/                    # Main Spring Boot application
├── ml-service/             # Python credit risk microservice
├── doc/                    # Project documentation (guides & explanations)
├── k8s/                    # Kubernetes manifests
├── terraform/              # Infrastructure as code
├── scripts/                # Utility scripts
├── Dockerfile
└── docker-compose.yml
```

### Documentation (`doc/`)

| File                            | Description                               |
| ------------------------------- | ----------------------------------------- |
| `Charge_and_Repayment_Logic.md` | How charges and repayments are processed  |
| `circuit_breaker.md`            | Circuit breaker pattern with Resilience4j |
| `DEVOPS_GUIDE.md`               | Docker, CI/CD, and deployment guide       |
| `KUBERNETES_EXPLAINED.md`       | Kubernetes setup and concepts             |
| `TERRAFORM_EXPLAINED.md`        | Terraform infrastructure guide            |

## Getting Started

**Prerequisites:** Java 21, Maven, MySQL 8, Redis

1. Clone the repo and create the database:

```sql
CREATE DATABASE loanms;
```

2. Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loanms
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Run:

```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`.

## API Overview

All loan, customer, repayment, and schedule endpoints require a `Bearer` token. Auth and health endpoints are public.

### Authentication

```http
POST /auth/register    # Register a new user
POST /auth/login       # Login and receive access + refresh tokens
POST /auth/refresh     # Refresh access token
POST /auth/logout      # Logout
```

**Login response:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer"
  }
}
```

Token validity: **access token = 15 min**, **refresh token = 7 days**.

### Customers

```http
POST   /customers                          # Create customer
GET    /customers/{id}                     # Get by internal ID
GET    /customers/customer-id/{customerId} # Get by customer ID (e.g. CUST-20240127-00001)
PUT    /customers/{id}                     # Update customer
DELETE /customers/{id}                     # Delete customer
GET    /customers?page=0&size=10           # Paginated list
```

**Create request:**

```json
{ "name": "Shoaib Shaikh", "email": "shoaib@example.com" }
```

### Loans

```http
POST /loans                        # Create loan
GET  /loans/{loanId}               # Get loan details
POST /loans/{loanId}/foreclose     # Foreclose a loan
```

**Create request (fixed rate):**

```json
{
  "customerId": 1,
  "principal": 100000.0,
  "interestRate": 12.5,
  "interestType": "FIXED",
  "tenureMonths": 24,
  "monthlyIncome": 50000.0,
  "creditScore": 750,
  "employmentStatus": "SALARIED",
  "existingDebt": 0.0,
  "loanPurpose": "HOME"
}
```

**Create request (stepped rate):**

```json
{
  "customerId": 1,
  "principal": 200000,
  "tenureMonths": 12,
  "interestType": "STEP",
  "interestRate": 0.0,
  "steppedRates": { "1": 8.0, "7": 10.0 },
  "monthlyIncome": 80000.0,
  "creditScore": 700,
  "employmentStatus": "SELF_EMPLOYED",
  "existingDebt": 15000.0,
  "loanPurpose": "BUSINESS"
}
```

Supported interest types: `FIXED`, `FLOATING`, `STEP`.

### Repayments

```http
POST /loans/{loanId}/repayments   # Record a payment
GET  /loans/{loanId}/repayments   # Get payment history
```

**Request:**

```json
{
  "amountPaid": 5000.0,
  "paymentDate": "2024-02-15",
  "mode": "UPI",
  "transactionId": "TXN123456789"
}
```

### Schedule

```http
GET /loans/{loanId}/schedule
PUT /loans/{loanId}/schedule/rate-change?newInterestRate=13.5&effectiveFromInstallment=6
```

### Charges

```http
POST   /loans/{loanId}/charges             # Add a charge
GET    /loans/{loanId}/charges             # List all charges
DELETE /loans/{loanId}/charges/{chargeId}  # Remove a charge
```

**Request:**

```json
{
  "type": "LATE_FEE",
  "amount": 500.0,
  "appliedDate": "2024-02-20",
  "description": "Late payment fee for installment 2"
}
```

### Health

```http
GET /health
```

## ML Credit Risk Service

Located in `ml-service/`. A Flask API that uses a pre-trained Random Forest model (`credit_risk_model.pkl`) to predict loan approval probability based on income, credit score, loan amount, employment status, and existing debt.

Run it separately:

```bash
cd ml-service
pip install -r requirements.txt
python app.py
```

## Docker / Kubernetes

Use `docker-compose.yml` to run the app, MySQL, and Redis together. Kubernetes manifests are in `k8s/`. See `doc/DEVOPS_GUIDE.md` and `doc/KUBERNETES_EXPLAINED.md` for details.
