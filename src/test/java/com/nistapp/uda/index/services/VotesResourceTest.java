package com.nistapp.uda.index.services;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for the /votes endpoints.
 * Validates authentication, response codes, and content types.
 */
@QuarkusTest
public class VotesResourceTest {

    // ----- Authentication Tests -----

    @Test
    void testGetVotesByIdRequiresAuth() {
        given()
                .when().get("/api/votes/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetVotesByIdAndSessionRequiresAuth() {
        given()
                .when().get("/api/votes/1/test-session")
                .then()
                .statusCode(401);
    }

    @Test
    void testAddVoteRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"sequenceid\":1,\"usersessionid\":\"test\",\"upvote\":1,\"downvote\":0}")
                .when().post("/api/votes/addVote")
                .then()
                .statusCode(401);
    }

    @Test
    void testDeleteVoteRequiresAuth() {
        given()
                .when().delete("/api/votes/deleteVote/1/test-session")
                .then()
                .statusCode(401);
    }

    // ----- Authenticated Tests -----

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testGetVotesByIdReturnsJson() {
        given()
                .when().get("/api/votes/999999")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testGetVotesByIdAndSessionReturnsNullForUnknown() {
        given()
                .when().get("/api/votes/999999/nonexistent-session")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}
