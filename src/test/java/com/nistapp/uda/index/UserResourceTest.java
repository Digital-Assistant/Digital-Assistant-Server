package com.nistapp.uda.index;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Contract tests for the /user endpoints.
 * Validates session key generation and authentication data endpoints.
 */
@QuarkusTest
public class UserResourceTest {

    @Test
    void testGetSessionKeyReturnsCookie() {
        given()
                .when().get("/api/user/getsessionkey")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .header("Set-Cookie", containsString("nist-voice-usersessionid"));
    }

    @Test
    void testGetSessionKeyWithExistingCookieReturnsSameValue() {
        String sessionKey = "existing-session-123";
        given()
                .cookie("nist-voice-usersessionid", sessionKey)
                .when().get("/api/user/getsessionkey")
                .then()
                .statusCode(200)
                .body(equalTo(sessionKey));
    }

    @Test
    void testCheckAuthIdAcceptsJson() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"test-auth-123\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("authid", equalTo("test-auth-123"))
                .body("authsource", equalTo("google"));
    }

    @Test
    void testCheckUserSessionAcceptsJson() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"test-session-456\",\"userauthid\":1}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testCheckAuthIdRejectsBlankAuthId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(400);
    }
}
