package com.nistapp.uda.index.services;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for the /sequence endpoints.
 * Validates authentication and response codes.
 */
@QuarkusTest
public class SequenceListServiceTest {

    @Test
    void testUpdateSequenceRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":1,\"usersessionid\":\"test\"}")
                .when().post("/api/sequence/update")
                .then()
                .statusCode(401);
    }

    @Test
    void testReindexSequenceRequiresAuth() {
        given()
                .when().post("/api/sequence/reindex/1")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testReindexNonexistentSequenceReturns404() {
        given()
                .when().post("/api/sequence/reindex/999999")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("success", equalTo(false))
                .body("message", equalTo("Sequence not found"));
    }
}
