package com.nistapp.uda.index;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestSecurity(user = "testUser", roles = { "user", "admin" })
public class StatusResourceTest {

    @Test
    public void testGetAllStatus() {
        given()
                .when().get("/api/status/all")
                .then()
                .statusCode(200)
                // Expecting an array, even if empty
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    public void testGetStatusByCategory() {
        given()
                .pathParam("category", "SYSTEM")
                .when().get("/api/status/category/{category}")
                .then()
                .statusCode(200);
    }
}
