package com.nistapp.uda.index.config;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(RequireRealmFilterTest.RealmRequired.class)
class RequireRealmFilterTest {

    public static class RealmRequired implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("udan.realm.required", "true");
        }
    }

    @Test
    void missingHeaderIsRejected() {
        given().when().get("/api/user/getsessionkey")
                .then().statusCode(400).body("error", containsString("UDAN-Realm"));
    }

    @Test
    void emptyHeaderIsRejected() {
        given().header("UDAN-Realm", " ").when().get("/api/user/getsessionkey")
                .then().statusCode(400);
    }

    @Test
    void missingHeaderIsRejectedBeforeAuthentication() {
        // an authenticated endpoint without token and without realm: 400, not 401
        given().when().get("/api/search/all").then().statusCode(400);
    }

    @Test
    void headerAllowsTheRequest() {
        given().header("UDAN-Realm", "UDAN-Plugin").when().get("/api/user/getsessionkey")
                .then().statusCode(200);
    }

    @Test
    void preflightIsNotRejected() {
        int status = given().when().options("/api/user/getsessionkey").statusCode();
        assertNotEquals(400, status);
    }

    @Test
    void pathsOutsideApiAreNotRejected() {
        int status = given().when().get("/q/health/live").statusCode();
        assertNotEquals(400, status);
    }

    @Test
    void ruleIsPureAndSmall() {
        assertTrue(RequireRealmFilter.isRejected("GET", "/api/x", null));
        assertTrue(RequireRealmFilter.isRejected("POST", "/api/x", ""));
        assertFalse(RequireRealmFilter.isRejected("OPTIONS", "/api/x", null));
        assertFalse(RequireRealmFilter.isRejected("GET", "/q/health", null));
        assertFalse(RequireRealmFilter.isRejected("GET", "/api/x", "R"));
    }
}
