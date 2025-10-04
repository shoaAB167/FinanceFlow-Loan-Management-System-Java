package com.finance.loanms.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
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

/**
 * Full integration test for AuthController.
 * - Spins up a MySQL Testcontainer
 * - Starts Spring Boot app on a random port
 * - Uses RestAssured for HTTP calls & assertions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // enables automatic lifecycle management for containers
public class AuthenticationFlowIntegrationTest {

    @ServiceConnection
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("loan_test")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void testRegister_Success() {
        Map<String, String> requestBody = Map.of(
                "username", "john_johny",
                "password", "Password1234"
        );

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(200) // must return OK
                .body("success", Matchers.equalTo(true))
                .body("message", Matchers.equalTo("User registered successfully"));
    }

    @Test
    void testLogin_Success() {
        Map<String, String> registerBody = Map.of("username", "john_login", "password", "Password123");
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200) // ensure user was created
                .body("success", Matchers.equalTo(true));

        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", Matchers.equalTo(true))
                .body("message", Matchers.equalTo("Login successful"));
    }

    @Test
    void testLogin_InvalidPassword() {
        // Setup: register user first
        Map<String, String> registerBody = Map.of("username", "john_invalid", "password", "Password123");
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body("success", Matchers.equalTo(true));

        // Attempt login with wrong password
        Map<String, String> loginBody = Map.of("username", "john_invalid", "password", "wrongpass");

        given()
                .contentType("application/json")
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.equalTo("Invalid username or password"));
    }

    @Test
    void testRegister_DuplicateUsername() {
        // First registration
        Map<String, String> registerBody = Map.of("username", "duplicate_user", "password", "Pssword123");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", Matchers.equalTo(true));

        // Attempt to register with same username
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.containsString("already exists"));
    }

    @Test
    void testRegister_InvalidUsername() {
        // Test empty username
        Map<String, String> registerBody = Map.of("username", "", "password", "pass123");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.containsString("Username is required"));

        // Test username with spaces only
        registerBody = Map.of("username", "   ", "password", "pass123");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));
    }

    @Test
    void testRegister_InvalidPassword() {
        // Test empty password
        Map<String, String> registerBody = Map.of("username", "test_user", "password", "");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.containsString("Password is required"));

        // Test password that's too short
        registerBody = Map.of("username", "test_user", "password", "123");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));
    }

    @Test
    void testLogin_NonExistentUser() {
        Map<String, String> loginBody = Map.of("username", "nonexistent_user", "password", "anypassword");
        
        given()
                .contentType("application/json")
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(404)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.equalTo("Invalid username or password"));
    }

    @Test
    void testLogin_MissingFields() {
        // Missing password
        Map<String, String> loginBody = Map.of("username", "test_user");
        
        given()
                .contentType("application/json")
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));

        // Missing username
        loginBody = Map.of("password", "test123");
        
        given()
                .contentType("application/json")
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));

        // Empty body
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));
    }

    @Test
    void testRegister_MissingFields() {
        // Missing password
        Map<String, String> registerBody = Map.of("username", "test_user");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));

        // Missing username
        registerBody = Map.of("password", "test123");
        
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));

        // Empty body
        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("success", Matchers.equalTo(false));
        }
    }
