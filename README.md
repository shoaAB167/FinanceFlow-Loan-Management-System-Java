# Finance Flow Loan Management System

A fintech-grade loan management backend using Java 17 + Spring Boot + MySQL + Redis.

## Build
```bash
mvn clean package

## 🔐 Authentication API (JWT-Based)

This project uses stateless JWT with refresh token support.

### Endpoints

| Method | Endpoint          | Description                  |
|--------|-------------------|------------------------------|
| POST   | `/auth/register`  | Register new user            |
| POST   | `/auth/login`     | Generate access + refresh JWT|
| POST   | `/auth/refresh`   | Refresh access token         |

### Response Format

All responses follow a standard format:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "...",
    "refreshToken": "..."
  }
}

Errors return structured messages:

{
  "success": false,
  "message": "Invalid password",
  "data": null
}
