package com.nistapp.uda.index.users;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Contract tests for /admin endpoint.
 * Validates authentication requirement and authorized access.
 */
@QuarkusTest
public class AdminResourceTest {

    @Test
    void testAdminRequiresAuth() {
        given()
                .when().get("/api/admin")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = { "admin" })
    void testAdminReturnsGranted() {
        given()
                .when().get("/api/admin")
                .then()
                .statusCode(200)
                .body(equalTo("granted"));
    }
}
