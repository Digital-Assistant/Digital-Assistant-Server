package com.nistapp.uda.index.config;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Every /api/ path needs a token (application.properties, quarkus.http.auth.permission.*), except the session key,
 * the legacy login calls (UserResourceTest) and CORS preflight. Health stays open.
 */
@QuarkusTest
public class ApiAuthenticationTest {

	@ParameterizedTest
	@CsvSource({
			"GET, /api/domain",
			"GET, /api/domain/patterns?domain=example.com",
			"GET, /api/search/links?query=x&domain=example.com",
			"GET, /api/search/all?query=x&domain=example.com&page=0&userSessionId=x",
			"GET, /api/search/withPermissions?query=x&domain=example.com&page=0&userSessionId=x",
			"GET, /api/status/all",
			"GET, /api/clickevents/all",
			"GET, /api/votes/1",
			"GET, /api/users/me",
			"POST, /api/clickevents/sequence/delete",
			"POST, /api/sequence/update",
			"DELETE, /api/votes/deleteVote/1/x",
	})
	void anonymousIsRefused(String method, String path) {
		given()
				.contentType("application/json")
				.body("{}")
				.when().request(method, path)
				.then()
				.statusCode(401);
	}

	@Test
	void corsPreflightNeedsNoToken() {
		given()
				// Same-origin: the test profile sets no CORS origins; dev/TEST/PROD allow the extension origin.
				.header("Origin", "http://localhost:8081")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "content-type,authorization,udan-realm")
				.when().options("/api/clickevents/sequence/delete")
				.then()
				.statusCode(200)
				.header("Access-Control-Allow-Origin", notNullValue());
	}

	@Test
	void healthNeedsNoToken() {
		given()
				.when().get("/q/health/live")
				.then()
				.statusCode(200);
	}
}
