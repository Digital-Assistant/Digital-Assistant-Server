package com.nistapp.uda.index;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * With {@code udan.auth.legacy-anonymous-login=false} (the setting for after PK15), the legacy login calls refuse
 * anonymous callers too. The session key stays public.
 */
@QuarkusTest
@TestProfile(StrictAnonymousLoginTest.Strict.class)
public class StrictAnonymousLoginTest {

    public static class Strict implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("udan.auth.legacy-anonymous-login", "false");
        }
    }

    @Test
    void anonymousCheckAuthIdIsRefused() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"x\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(401);
    }

    @Test
    void anonymousCheckUserSessionIsRefused() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"x\",\"userauthid\":1}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(401);
    }

    @Test
    void sessionKeyStaysPublic() {
        given()
                .when().get("/api/user/getsessionkey")
                .then()
                .statusCode(200);
    }
}
