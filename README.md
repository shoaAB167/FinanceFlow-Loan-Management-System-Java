# FinanceFlow - Loan Management System

## 📋 Overview

This is a development project demonstrating Spring Boot capabilities for loan management systems. For production use, ensure proper security hardening, comprehensive testing, and performance optimization.
functionalities for managing loan accounts, repayments, schedules, and customer data. The system supports various loan types with different interest calculation methods and provides a robust API for loan lifecycle management.

## 🏗️ Application Architecture  Flow

### Core Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │    Services     │    │  Repositories   │
│                 │    │                 │    │                 │
│ • AuthController│───▶│ • UserService   │───▶│ • UserRepository│
│ • LoanController│    │ • LoanService   │    │ • LoanRepository│
│ • RepaymentCtrl │    │ • RepaymentSvc  │    │ • RepaymentRepo │
│ • ScheduleCtrl  │    │ • ScheduleSvc   │    │ • InstallmentRep│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Security      │    │   Data Models   │    │    Database     │
│                 │    │                 │    │                 │
│ • JWT Auth      │    │ • LoanAccount   │    │ • MySQL         │
│ • Spring Sec    │    │ • Customer      │    │ • JPA/Hibernate │
│ • Role-based    │    │ • Repayment     │    │ • Redis Cache   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Application Flow

#### 1. **User Authentication Flow**
```
User Registration → JWT Token Generation → Login → Access Protected APIs
```

#### 2. **Loan Creation Flow**
```
Customer Data → Loan Request → Validation → Schedule Generation → Loan Account Creation
```

#### 3. **Repayment Flow**
```
Payment Request → Amount Validation → Schedule Update → Payment Recording → Balance Calculation
```

#### 4. **Loan Closure Flow**
```
Foreclosure Request → Outstanding Calculation → Final Payment → Status Update → Account Closure
```

## 🚀 Key Features

### ✅ **Implemented Features**
- **User Authentication & Authorization** (JWT-based)
- **Customer Management** (✅ **COMPLETED** - Create, Read, Update, Delete, List with pagination)
- **Loan Account Management** (Create, Read, Foreclose)
- **Repayment Processing** (Payment recording and tracking)
- **Interest Calculation** (Fixed, Floating, Step-down rates)
- **Loan Status Tracking** (Active, Closed, Foreclosed)
- **Schedule Management** (✅ **COMPLETED** - Get schedule, Update rate changes)
- **Charge Management** (✅ **COMPLETED** - Add, View, Remove charges)
- **Interest Rate Changes** (✅ **COMPLETED** - Dynamic rate updates)
- **Audit Trail** (Created/Updated timestamps for all entities)
- **Data Validation** (Bean validation for all request payloads)
- **Global Exception Handling**
- **RESTful API Design**

## 🔧 Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0
- **Cache**: Redis
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **Additional**: Lombok, Jackson, Bean Validation

1. **Clone the repository**
```bash
git clone repository-url
cd FinanceFlow-Loan-Management-System-Java
```

2. **Database Setup**
```sql
CREATE DATABASE loanms;
```

3. **Update Configuration**
   Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loanms
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **Run the Application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation Testing Guide

### Authentication APIs

#### 1. User Registration
```http
POST /auth/register
Content-Type: application/json

{
  "username": "shoaib shaikh",
  "password": "pass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": "Registration successful"
}
```

#### 2. User Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "shoaib shaikh",
  "password": "pass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  }
}
```

#### 3. Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 4. Logout
```http
POST /auth/logout
Authorization: Bearer access_token
```

### Customer Management APIs

#### 1. Create Customer
```http
POST /customers
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "sahil mulla",
  "email": "sahil@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "customerId": "CUST-20240127-00001",
    "name": "sahil mulla",
    "email": "sahil@example.com",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "loans": []
  }
}
```

#### 2. Get Customer by ID
```http
GET /customers/{customerId}
Authorization: Bearer <access_token>
```

#### 3. Get Customer by Customer ID
```http
GET /customers/customer-id/{customerIdentifier}
Authorization: Bearer <access_token>
```

#### 4. Update Customer
```http
PUT /customers/{customerId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john@example.com"
}
```

#### 5. Delete Customer
```http
DELETE /customers/{customerId}
Authorization: Bearer <access_token>
```

#### 6. Get All Customers (with pagination)
```http
GET /customers?page=0&size=10&sort=name,asc
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Customers retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "customerId": "CUST001",
        "name": "John Doe",
        "email": "john@example.com",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00",
        "loans": [
          {
            "loanId": 1,
            "loanAccountId": "LOAN-2024-001",
            "principal": 50000.0,
            "status": "ACTIVE",
            "tenureMonths": 12
          }
        ]
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### Loan Management APIs

#### 1. Create Loan Account
```http
POST /loans
Authorization: Bearer access_token
Content-Type: application/json

{
  "customerId": 1,
  "principal": 100000.00,
  "interestRate": 12.5,
  "interestType": "FIXED",
  "tenureMonths": 24
}

{
  "customerId": 1,
  "principal": 200000,
  "tenureMonths": 12,
  "interestType": "STEP",
  "interestRate": 0.0,
  "steppedRates": {
    "1": 8.0,
    "7": 10.0
  }
}

```

**Response:**
```json
{
  "success": true,
  "message": "Loan created successfully",
  "data": {
    "loanId": "LOAN-2024-001",
    "customerId": 1,
    "principal": 100000.00,
    "interestRate": 12.5,
    "interestType": "FIXED",
    "tenureMonths": 24,
    "status": "ACTIVE",
    "startDate": "2024-01-15"
  }
}
```

#### 2. Get Loan Details
```http
GET /loans/{loanId}
Authorization: Bearer access_token
```

#### 3. Foreclose Loan
```http
POST /loans/{loanId}/foreclose
Authorization: Bearer access_token
Content-Type: application/json

{
  "foreclosureDate": "2024-12-31"
}
```

### Repayment APIs

#### 1. Make Repayment
```http
POST /loans/{loanId}/repayments
Authorization: Bearer access_token
Content-Type: application/json

{
  "amountPaid": 5000.00,
  "paymentDate": "2024-02-15",
  "mode": "UPI",
  "transactionId": "TXN123456789"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Repayment processed successfully",
  "data": {
    "repaymentId": "RPY-2024-001",
    "loanId": "LOAN-2024-001",
    "amountPaid": 5000.00,
    "paymentDate": "2024-02-15",
    "mode": "UPI",
    "transactionId": "TXN123456789",
    "outstandingBalance": 95000.00
  }
}
```

#### 2. Get Repayment History
```http
GET /loans/{loanId}/repayments
Authorization: Bearer access_token
```

### Schedule APIs

#### 1. Get Loan Schedule
```http
GET /loans/{loanId}/schedule
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Schedule retrieved successfully",
  "data": {
    "loanId": 1,
    "schedule": [
      {
        "installmentNumber": 1,
        "dueDate": "2024-02-15",
        "principalComponent": 4166.67,
        "interestComponent": 1041.67,
        "totalAmount": 5208.34,
        "status": "DUE"
      }
    ]
  }
}
```

#### 2. Update Schedule After Rate Change
```http
PUT /loans/{loanId}/schedule/rate-change
Authorization: Bearer <access_token>

?newInterestRate=13.5&effectiveFromInstallment=6
```

### Charge Management APIs

#### 1. Add Charge to Loan
```http
POST /loans/{loanId}/charges
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "type": "LATE_FEE",
  "amount": 500.00,
  "appliedDate": "2024-02-20",
  "description": "Late payment fee for installment 2"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Charge added successfully",
  "data": {
    "chargeId": 1,
    "type": "LATE_FEE",
    "amount": 500.00,
    "appliedDate": "2024-02-20",
    "description": "Late payment fee for installment 2",
    "loanAccountId": 1
  }
}
```

#### 2. Get All Charges for Loan
```http
GET /loans/{loanId}/charges
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Charges retrieved successfully",
  "data": {
    "loanAccountId": 1,
    "charges": [
      {
        "chargeId": 1,
        "type": "LATE_FEE",
        "amount": 500.00,
        "appliedDate": "2024-02-20",
        "description": "Late payment fee for installment 2"
      }
    ],
    "totalCharges": 500.00
  }
}
```

#### 3. Remove Charge from Loan
```http
DELETE /loans/{loanId}/charges/{chargeId}
Authorization: Bearer <access_token>
```

### Health Check API

```http
GET /health
```

## 🔒 Security Configuration

The application uses JWT-based authentication with the following configuration:
- **Access Token Validity**: 15 minutes
- **Refresh Token Validity**: 7 days
- **Protected Endpoints**: All loan, repayment, and schedule APIs
- **Public Endpoints**: Authentication APIs and health check

## 📊 Database Schema

### Key Entities
- **User**: System users with authentication
- **Customer**: Loan customers
- **LoanAccount**: Main loan entity
- **Installment**: Payment schedule
- **Repayment**: Payment records
- **Charge**: Additional charges
- **InterestRate**: Embedded interest configuration

## 🚀 Future Enhancements

1. **Complete Schedule Management**
2. **Add Swagger/OpenAPI Documentation**
3. **Implement Comprehensive Testing**
4. **Add Notification System**
5. **Create Reporting Dashboard**
6. **Add Rate Limiting**
7. **Implement Audit Logging**
8. **Add Docker Support**
9. **Create CI/CD Pipeline**
10. **Add Monitoring  Metrics**

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

