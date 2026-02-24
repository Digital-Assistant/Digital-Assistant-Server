package com.nistapp.uda.index.config;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the SecurityHeadersFilter, verifying OWASP security headers are
 * present.
 */
@QuarkusTest
@TestSecurity(user = "testUser", roles = { "user" })
public class SecurityHeadersFilterTest {

    @Test
    void testXContentTypeOptionsHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    void testXFrameOptionsHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("X-Frame-Options", "DENY");
    }

    @Test
    void testXXssProtectionHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("X-XSS-Protection", "1; mode=block");
    }

    @Test
    void testStrictTransportSecurityHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("Strict-Transport-Security", containsString("max-age="));
    }

    @Test
    void testContentSecurityPolicyHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("Content-Security-Policy", "default-src 'self'");
    }

    @Test
    void testReferrerPolicyHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    void testPermissionsPolicyHeader() {
        given()
                .when().get("/api/domain/patterns")
                .then()
                .header("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    }
}
