package com.nistapp.uda.index.services;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for the /clickevents endpoints.
 * Validates authentication requirements, response codes, and content types.
 */
@QuarkusTest
public class ClickeventsResourceTest {

    // ----- Authentication Tests -----

    @Test
    void testAllEndpointRequiresAuth() {
        given()
                .when().get("/api/clickevents/all")
                .then()
                .statusCode(401);
    }

    @Test
    void testFetchByUrlRequiresAuth() {
        given()
                .queryParam("url", "http://example.com")
                .when().get("/api/clickevents/fetchbyurl")
                .then()
                .statusCode(401);
    }

    @Test
    void testFetchRecordDataRequiresAuth() {
        given()
                .queryParam("start", 0L)
                .queryParam("end", System.currentTimeMillis())
                .queryParam("sessionid", "test-session")
                .queryParam("domain", "example.com")
                .when().get("/api/clickevents/fetchrecorddata")
                .then()
                .statusCode(401);
    }

    @Test
    void testClickedNodeRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/clickevents/clickednode")
                .then()
                .statusCode(401);
    }

    @Test
    void testRecordSequenceDataRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/clickevents/recordsequencedata")
                .then()
                .statusCode(401);
    }

    @Test
    void testDeleteSequenceRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/clickevents/sequence/delete")
                .then()
                .statusCode(401);
    }

    @Test
    void testUserClickRequiresAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().put("/api/clickevents/userclick")
                .then()
                .statusCode(401);
    }

    @Test
    void testSuggestedRequiresAuth() {
        given()
                .queryParam("domain", "example.com")
                .when().get("/api/clickevents/suggested")
                .then()
                .statusCode(401);
    }

    // ----- Authenticated Endpoint Tests -----

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testGetAllReturnsJson() {
        given()
                .when().get("/api/clickevents/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testFetchByUrlReturnsJson() {
        given()
                .queryParam("url", "http://nonexistent.example.com/page")
                .when().get("/api/clickevents/fetchbyurl")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testGetTimestampReturnsNumber() {
        given()
                .when().get("/api/clickevents/fetchtimestamp")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testGetSequenceVotesReturnsJson() {
        given()
                .when().get("/api/clickevents/sequence/votes/999999")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "user" })
    void testSuggestedWithDomainReturnsJson() {
        given()
                .queryParam("domain", "example.com")
                .when().get("/api/clickevents/suggested")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
