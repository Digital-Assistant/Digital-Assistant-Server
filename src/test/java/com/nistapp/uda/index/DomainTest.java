package com.nistapp.uda.index;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class DomainTest {

    @Test
    public void testDefaultResponse() {
        given()
                .when().get("/api/domain")
                .then()
                .statusCode(200)
                .body(is("Site is up"));
    }

    @Test
    public void testDomainPatterns() {
        // Just checking contract/status since it requires a DB connection for real data
        given()
                .queryParam("domain", "example.com")
                .when().get("/api/domain/patterns")
                .then()
                .statusCode(200);
    }
}
