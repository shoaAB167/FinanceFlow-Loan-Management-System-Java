package com.finance.loanms.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Full integration test for Loan Account Flow.
 * - Start MySQL Testcontainer
 * - Starts Spring Boot app on a random port
 * - Uses RestAssured for HTTP calls & assertions
 * - Handles authentication flow automatically
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class LoanAccountFlowIntegrationTest {

    @ServiceConnection
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("loan_test")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    private String authToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        Map<String, String> registerBody = Map.of(
                "username", "user1" + System.currentTimeMillis(),
                "password", "Password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(200);

        var loginResponse = given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("data");

        authToken = "Bearer " + loginResponse.get("accessToken");
    }

    @Test
    void createLoanAccount_ValidFixedInterest_ReturnsCreatedLoan() {
        Long customerId = createTestCustomer();
        String requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": 10000,
                    "tenureMonths": 12,
                    "interestType": "FIXED",
                    "interestRate": 10
                }
                """, customerId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.principal", equalTo(10000.00f))
                .body("data.interestRate", equalTo(10f))
                .body("data.tenureMonths", equalTo(12))
                .body("data.interestType", equalTo("FIXED"))
                .body("data.customerId", notNullValue())
                .body("data.status", equalTo("ACTIVE"));
    }
    
    @Test
    void createLoanAccount_ValidStepInterest_ReturnsCreatedLoan() {
        Long customerId = createTestCustomer();
        String requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": 15000,
                    "tenureMonths": 6,
                    "interestType": "STEP",
                    "steppedRates": {
                        "1": 8.0,
                        "3": 9.5,
                        "5": 10.5
                    }
                }
                """, customerId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.principal", equalTo(15000.00f))
                .body("data.tenureMonths", equalTo(6))
                .body("data.interestType", equalTo("STEP"))
                .body("data.customerId", notNullValue())
                .body("data.status", equalTo("ACTIVE"));
    }
    
    @Test
    void createLoanAccount_MissingInterestRateForFixed_ReturnsError() {
        Long customerId = createTestCustomer();
        String requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": 10000,
                    "tenureMonths": 12,
                    "interestType": "FIXED"
                }
                """, customerId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans")
                .then()
                .log().all()
                .statusCode(500)
                .body("success", equalTo(false))
                .body("message", containsString("Something went wrong: java.lang.IllegalArgumentException: Interest rate is required for FIXED interest type"));
    }

    @Test
    void createLoanAccount_InvalidCustomer_ReturnsNotFound() {
        String invalidRequestBody = """
             {
                "customerId": 999999,
                "principal": 10000,
                "tenureMonths": 12,
                "interestType": "FIXED",
                "interestRate": 10
             }
             """;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(invalidRequestBody)
                .when()
                .post("/loans")
                .then()
                .log().all()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("message", containsString("Customer not found with ID: 999999"));
    }
    
    @Test
    void forecloseLoan_WithUnpaidEMIs_ReturnsError() {
        Long customerId = createTestCustomer();
        Long loanId = createTestLoan(customerId, 10000, 12, "FIXED", 10.0, null);

        String requestBody = """
                {
                    "foreclosureDate": "2025-12-31"
                }
                """;
                
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans/" + loanId + "/foreclose")
                .then()
                .log().all()
                .statusCode(500)
                .body("success", equalTo(false))
                .body("message", containsString("Loan cannot be foreclosed — unpaid installments exist"));
    }
    
    @Test
    void forecloseLoan_ValidRequest_ReturnsSuccess() {
        Long customerId = createTestCustomer();
        Long loanId = createTestLoan(customerId, 12000, 12, "FIXED", 10.0, null);

        // Get the actual EMI amount from the loan schedule
        Number emiAmountNumber = given()
                .header("Authorization", authToken)
                .when()
                .get("/loans/" + loanId + "/schedule")
                .then()
                .statusCode(200)
                .extract()
                .path("data.schedule[0].totalAmount");
        
        Double emiAmount = emiAmountNumber.doubleValue();

        payAllEMIs(loanId, 12, emiAmount);

        String requestBody = """
                {
                    "foreclosureDate": "2025-12-31"
                }
                """;
                
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans/" + loanId + "/foreclose")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.status", equalTo("FORECLOSED"));
    }
    
    @Test
    void getLoanById_ValidId_ReturnsLoan() {
        Long customerId = createTestCustomer();
        Long loanId = createTestLoan(customerId, 10000, 12, "FIXED", 10.0, null);

        given()
                .header("Authorization", authToken)
                .when()
                .get("/loans/" + loanId)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.loanId", equalTo(loanId.intValue()))
                .body("data.principal", equalTo(10000.00f))
                .body("data.status", equalTo("ACTIVE"));
    }
    
    @Test
    void getLoanById_InvalidId_ReturnsNotFound() {
        given()
                .header("Authorization", authToken)
                .when()
                .get("/loans/999999")
                .then()
                .log().all()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("message", containsString("Loan not found with ID: 999999"));
    }

    @Test
    void createAndRetrieveLoan_Flow_Success() {
        Long customerId = createTestCustomer();

        String requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": 10000,
                    "tenureMonths": 12,
                    "interestType": "FIXED",
                    "interestRate": 10
                }
                """, customerId);

        Integer loanId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.loanId", notNullValue())
                .extract()
                .path("data.loanId");

        given()
                .header("Authorization", authToken)
                .when()
                .get("/loans/" + loanId)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.loanId", equalTo(loanId))
                .body("data.principal", equalTo(10000.00f))
                .body("data.status", equalTo("ACTIVE"));
    }

    private Long createTestCustomer() {
        String uniqueEmail = String.format("customer%d@test.com", System.currentTimeMillis());
        String customerRequest = String.format("""
                {
                    "name": "Test Customer",
                    "email": "%s"
                }
                """, uniqueEmail);

        Integer customerId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(customerRequest)
                .when()
                .post("/customers")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("data.id");

        return customerId.longValue();
    }
    
    private Long createTestLoan(Long customerId, double principal, int tenureMonths, 
                              String interestType, Double interestRate, Map<Integer, Double> steppedRates) {
        String requestBody;
        
        if (interestType.equals("STEP")) {
            requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": %f,
                    "tenureMonths": %d,
                    "interestType": "%s",
                    "steppedRates": %s
                }
                """, customerId, principal, tenureMonths, interestType, mapToJson(steppedRates));
        } else {
            requestBody = String.format("""
                {
                    "customerId": %d,
                    "principal": %f,
                    "tenureMonths": %d,
                    "interestType": "%s",
                    "interestRate": %f
                }
                """, customerId, principal, tenureMonths, interestType, interestRate);
        }
        
        Integer loanId =  given()
                .contentType(ContentType.JSON)
                .header("Authorization", authToken)
                .body(requestBody)
                .when()
                .post("/loans")
                .then()
                .statusCode(200)
                .extract()
                .path("data.loanId");

        return loanId.longValue();
    }
    
    private String mapToJson(Map<Integer, Double> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            sb.append(String.format("\"%d\":%.1f,", entry.getKey(), entry.getValue()));
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private void payAllEMIs(Long loanId, int numberOfEMIs, Double emiAmount) {
        for (int i = 1; i <= numberOfEMIs; i++) {
            String transactionId = "TXN-" + loanId + "-" + System.currentTimeMillis() + "-" + i;
            String paymentBody = String.format("""
                {
                    "paymentDate": "2023-%02d-15",
                    "amountPaid": %.2f,
                    "mode": "CASH",
                    "transactionId": "%s"
                }
                """, i, emiAmount, transactionId);

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", authToken)
                    .body(paymentBody)
                    .when()
                    .post("/loans/" + loanId + "/repayments")
                    .then()
                    .log().all()
                    .statusCode(200);
        }
    }
}
