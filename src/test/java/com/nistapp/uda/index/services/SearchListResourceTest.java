package com.nistapp.uda.index.services;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Input validation tests for GET /api/search/all.
 */
@QuarkusTest
@TestSecurity(user = "testUser", roles = { "user" })
public class SearchListResourceTest {

    @Test
    void testSearchWithoutUserSessionIdReturns400() {
        given()
                .queryParam("domain", "example.com")
                .when().get("/api/search/all")
                .then()
                .statusCode(400);
    }

    @Test
    void testSearchWithBlankUserSessionIdReturns400() {
        given()
                .queryParam("domain", "example.com")
                .queryParam("userSessionId", " ")
                .when().get("/api/search/all")
                .then()
                .statusCode(400);
    }

    @Test
    void testSearchWithoutDomainReturns400() {
        given()
                .queryParam("userSessionId", "test-session")
                .when().get("/api/search/all")
                .then()
                .statusCode(400);
    }
}
