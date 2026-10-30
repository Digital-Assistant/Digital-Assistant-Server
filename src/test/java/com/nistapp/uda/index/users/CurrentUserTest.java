package com.nistapp.uda.index.users;

import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of the token-to-identity mapping in {@link CurrentUser#map}. No Quarkus needed.
 */
class CurrentUserTest {

	private static final List<String> LEGACY = List.of("backend-service");
	private static final List<String> PKCE = List.of("udan-extension");

	@Test
	void legacyClientMapsToPreferredUsernameAndGoogle() {
		CurrentUser.Identity id = CurrentUser.map("backend-service", "abc123", "kc-sub", LEGACY, PKCE);
		assertEquals("abc123", id.authid());
		assertEquals("google", id.authsource());
	}

	@Test
	void otherClientMapsToSubAndKeycloak() {
		CurrentUser.Identity id = CurrentUser.map("udan-extension", "someone", "kc-sub", LEGACY, PKCE);
		assertEquals("kc-sub", id.authid());
		assertEquals("keycloak", id.authsource());
	}

	@Test
	void missingAzpMapsToSubAndKeycloak() {
		assertEquals("keycloak", CurrentUser.map(null, "someone", "kc-sub", LEGACY, PKCE).authsource());
	}

	@Test
	void legacyClientWithoutPreferredUsernameFailsClosed() {
		assertThrows(NotAuthorizedException.class,
				() -> CurrentUser.map("backend-service", " ", "kc-sub", LEGACY, PKCE));
	}

	@Test
	void otherClientWithoutSubFailsClosed() {
		assertThrows(NotAuthorizedException.class, () -> CurrentUser.map("udan-extension", "x", null, LEGACY, PKCE));
	}

	@Test
	void legacyClientListIsConfigurable() {
		assertEquals("google", CurrentUser.map("other-legacy", "u", "s", List.of("a", "other-legacy"), PKCE)
				.authsource());
	}

	@Test
	void pkceClientMapsToSubAndKeycloakEvenWithALegacyLookingUsername() {
		// A Keycloak user can choose a username equal to an existing google authid; it must not matter.
		String googleAuthid = "0f".repeat(64);
		CurrentUser.Identity id = CurrentUser.map("udan-extension", googleAuthid, "kc-sub", LEGACY, PKCE);
		assertEquals("kc-sub", id.authid());
		assertEquals("keycloak", id.authsource());
	}

	@Test
	void pkceClientWinsWhenAlsoListedAsLegacy() {
		List<String> misconfigured = List.of("backend-service", "udan-extension");
		CurrentUser.Identity id = CurrentUser.map("udan-extension", "someone", "kc-sub", misconfigured, PKCE);
		assertEquals("keycloak", id.authsource());
		assertEquals("kc-sub", id.authid());
		assertEquals("google", CurrentUser.map("backend-service", "u", "s", misconfigured, PKCE).authsource());
	}

	@Test
	void pkceClientListIsConfigurable() {
		List<String> pkce = List.of("udan-extension-test");
		assertEquals("keycloak",
				CurrentUser.map("udan-extension-test", "u", "s", List.of("udan-extension-test"), pkce).authsource());
	}

	@Test
	void overlapBetweenTheListsIsReported() {
		assertEquals(List.of("udan-extension"),
				AuthClients.overlap(List.of("backend-service", "udan-extension"), PKCE));
		assertTrue(AuthClients.overlap(LEGACY, PKCE).isEmpty());
		assertTrue(AuthClients.overlap(null, PKCE).isEmpty());
	}

	@Test
	void onlyAllDigitValuesCountAsRowIds() {
		assertTrue(CurrentUser.isDigits("42"));
		assertFalse(CurrentUser.isDigits(""));
		assertFalse(CurrentUser.isDigits(null));
		assertFalse(CurrentUser.isDigits("4a2"));
		assertFalse(CurrentUser.isDigits("-42"));
		assertFalse(CurrentUser.isDigits("0f".repeat(64)));
	}
}
