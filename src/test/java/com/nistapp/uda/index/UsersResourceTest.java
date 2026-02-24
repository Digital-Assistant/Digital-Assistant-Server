package com.nistapp.uda.index;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class UsersResourceTest {

    @Test
    public void testGetUsersMeUnauthorized() {
        // Without a valid Keycloak token, this should return 401
        given()
                .when().get("/api/users/me")
                .then()
                .statusCode(401);
    }
}
