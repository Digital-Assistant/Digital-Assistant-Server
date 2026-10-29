package com.nistapp.uda.index.users;

import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests of the token-to-identity mapping in {@link CurrentUser#map}. No Quarkus needed.
 */
class CurrentUserTest {

	private static final List<String> LEGACY = List.of("backend-service");

	@Test
	void legacyClientMapsToPreferredUsernameAndGoogle() {
		CurrentUser.Identity id = CurrentUser.map("backend-service", "abc123", "kc-sub", LEGACY);
		assertEquals("abc123", id.authid());
		assertEquals("google", id.authsource());
	}

	@Test
	void otherClientMapsToSubAndKeycloak() {
		CurrentUser.Identity id = CurrentUser.map("udan-extension", "someone", "kc-sub", LEGACY);
		assertEquals("kc-sub", id.authid());
		assertEquals("keycloak", id.authsource());
	}

	@Test
	void missingAzpMapsToSubAndKeycloak() {
		assertEquals("keycloak", CurrentUser.map(null, "someone", "kc-sub", LEGACY).authsource());
	}

	@Test
	void legacyClientWithoutPreferredUsernameFailsClosed() {
		assertThrows(NotAuthorizedException.class, () -> CurrentUser.map("backend-service", " ", "kc-sub", LEGACY));
	}

	@Test
	void otherClientWithoutSubFailsClosed() {
		assertThrows(NotAuthorizedException.class, () -> CurrentUser.map("udan-extension", "x", null, LEGACY));
	}

	@Test
	void legacyClientListIsConfigurable() {
		assertEquals("google", CurrentUser.map("other-legacy", "u", "s", List.of("a", "other-legacy")).authsource());
	}
}
