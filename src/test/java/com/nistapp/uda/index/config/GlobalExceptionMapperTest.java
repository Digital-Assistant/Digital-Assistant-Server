package com.nistapp.uda.index.config;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests for GlobalExceptionMapper validating RFC 7807 Problem Details
 * responses.
 * Uses Bean Validation violations to trigger the mapper.
 */
@QuarkusTest
public class GlobalExceptionMapperTest {

    @Test
    @TestSecurity(user = "testUser", roles = { "user", "admin" })
    void testValidationErrorReturns400WithProblemDetails() {
        // Send a Status with blank name to trigger @NotBlank validation
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"category\":\"test\"}")
                .when().post("/api/status")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("title", equalTo("Constraint Violation"))
                .body("status", equalTo(400))
                .body("detail", equalTo("Input validation failed"))
                .body("violations", notNullValue());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user", "admin" })
    void testValidStatus() {
        // Send a valid Status to verify it's accepted
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Test Status\",\"category\":\"general\"}")
                .when().post("/api/status")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test Status"));
    }

    @Test
    void test404ReturnsJson() {
        given()
                .when().get("/api/nonexistent-endpoint-12345")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user", "admin" })
    void testValidationErrorOnUserAuthWithBlankId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"\",\"authsource\":\"test\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("title", equalTo("Constraint Violation"))
                .body("violations", notNullValue());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user", "admin" })
    void testValidationErrorIncludesTimestamp() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"category\":\"test\"}")
                .when().post("/api/status")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .body("timestamp", notNullValue());
    }
}
