package com.finance.loanms.controller;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("loan_test")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    static {
        mysql.start();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void testRegister_Success() {
        Map<String, String> requestBody = Map.of(
                "username", "john johny",
                "password", "pass1234"
        );

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", Matchers.equalTo(true))
                .body("message", Matchers.equalTo("User registered successfully"));
    }

    @Test
    void testLogin_Success() {
        Map<String, String> registerBody = Map.of("username", "john", "password", "pass123");
        given().contentType("application/json").body(registerBody).post("/auth/register");

        given()
                .contentType("application/json")
                .body(registerBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", Matchers.equalTo(true))
                .body("message", Matchers.equalTo("Login successful"))
                .body("data.accessToken", Matchers.notNullValue())
                .body("data.refreshToken", Matchers.notNullValue());
    }

    @Test
    void testLogin_InvalidPassword() {
        Map<String, String> registerBody = Map.of("username", "john", "password", "pass123");
        given().contentType("application/json").body(registerBody).post("/auth/register");

        Map<String, String> loginBody = Map.of("username", "john", "password", "wrongpass");

        given()
                .contentType("application/json")
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", Matchers.equalTo(false))
                .body("message", Matchers.equalTo("Invalid password"));
    }
}