package com.nistapp.uda.index.users;

import com.nistapp.uda.index.utils.LoggerUtil;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.List;

/**
 * Which Keycloak clients ({@code azp} claim) a token may come from, grouped by how {@link CurrentUser} maps it to a
 * stored identity. This is not an allow-list: a token of any client in a known realm is accepted (owner decision
 * Q9); {@code azp} only selects the mapping.
 * <ul>
 *     <li>{@code udan.auth.pkce-client-ids} (default {@code udan-extension}): public clients that sign in with
 *     authorization code + PKCE. Their users are Keycloak users, mapped to ({@code sub}, {@code keycloak}). Checked
 *     first, so a public client listed in both settings by mistake still maps by {@code sub}: on a public client
 *     the user, not the token service, controls {@code preferred_username}, and mapping it to a google
 *     {@code authid} would let them take over that user.</li>
 *     <li>{@code udan.auth.legacy-client-ids} (default {@code backend-service}): the confidential client the token
 *     service mints tokens for today, mapped to ({@code preferred_username}, {@code google}).</li>
 * </ul>
 * Any other client maps like a PKCE client.
 */
@ApplicationScoped
public class AuthClients {

	private static final Logger logger = LoggerUtil.getLogger(AuthClients.class);

	@ConfigProperty(name = "udan.auth.legacy-client-ids", defaultValue = "backend-service")
	List<String> legacyClientIds;

	@ConfigProperty(name = "udan.auth.pkce-client-ids", defaultValue = "udan-extension")
	List<String> pkceClientIds;

	void onStart(@Observes StartupEvent event) {
		List<String> both = overlap(legacyClientIds, pkceClientIds);
		if (!both.isEmpty()) {
			LoggerUtil.warn(logger, "client ids " + both + " are in both udan.auth.pkce-client-ids and"
					+ " udan.auth.legacy-client-ids; their tokens map by sub (keycloak), not as legacy users");
		}
	}

	public List<String> legacyClientIds() {
		return legacyClientIds;
	}

	public List<String> pkceClientIds() {
		return pkceClientIds;
	}

	static List<String> overlap(List<String> legacy, List<String> pkce) {
		if (legacy == null || pkce == null) {
			return List.of();
		}
		return legacy.stream().filter(pkce::contains).distinct().toList();
	}
}
