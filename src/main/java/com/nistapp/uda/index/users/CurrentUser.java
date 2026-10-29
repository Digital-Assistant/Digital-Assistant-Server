package com.nistapp.uda.index.users;

import com.nistapp.uda.index.models.UserAuthData;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import java.util.List;

/**
 * The caller of the current request, taken from the bearer token only, never from the request body or URL.
 * <p>
 * Mapping from token to the stored identity ({@code user_auth_data.authid}, {@code authsource}):
 * <ul>
 *     <li>a token issued to one of the legacy confidential clients ({@code azp} in
 *     {@code udan.auth.legacy-client-ids}, by default {@code backend-service}) maps to
 *     ({@code preferred_username}, {@code google}). The token service creates those Keycloak users with
 *     username = the extension's SHA-512 hex of the Google profile id, which is exactly the {@code authid}
 *     the extension stores with {@code authsource = google}, so existing users and recordings keep working;</li>
 *     <li>any other token maps to ({@code sub}, {@code keycloak}).</li>
 * </ul>
 * The matching {@code user_auth_data} row is found or created on first use and cached for the request.
 */
@RequestScoped
public class CurrentUser {

	static final String SOURCE_GOOGLE = "google";
	static final String SOURCE_KEYCLOAK = "keycloak";

	@Inject
	SecurityIdentity identity;

	@Inject
	UserAccountService userAccountService;

	@ConfigProperty(name = "udan.auth.legacy-client-ids", defaultValue = "backend-service")
	List<String> legacyClientIds;

	private Identity resolved;
	private UserAuthData authData;

	/**
	 * @return true when the request carries no (valid) token
	 */
	public boolean isAnonymous() {
		return identity == null || identity.isAnonymous();
	}

	/**
	 * @return the caller's (authid, authsource) pair
	 * @throws NotAuthorizedException when the request is anonymous or the token carries no usable identity
	 */
	public Identity identity() {
		if (resolved == null) {
			if (isAnonymous() || !(identity.getPrincipal() instanceof JsonWebToken jwt)) {
				throw new NotAuthorizedException("Bearer");
			}
			resolved = map(jwt.getClaim("azp"), jwt.getClaim("preferred_username"), jwt.getSubject(),
					legacyClientIds);
		}
		return resolved;
	}

	/**
	 * The caller's own {@code user_auth_data} row, created on first use.
	 *
	 * @param emailid stored only when a new row is created or the existing row has none (may be null)
	 * @return the caller's row (the oldest one when duplicates exist)
	 */
	public UserAuthData authData(String emailid) {
		if (authData == null) {
			Identity id = identity();
			authData = userAccountService.findOrCreate(id.authid(), id.authsource(), emailid);
		}
		return authData;
	}

	/**
	 * @return the caller's own {@code user_auth_data} row, created on first use
	 */
	public UserAuthData authData() {
		return authData(null);
	}

	/**
	 * Pure token-to-identity mapping, kept static so it can be unit tested without a token.
	 *
	 * @throws NotAuthorizedException when the claim the mapping needs is missing (fail closed)
	 */
	static Identity map(String azp, String preferredUsername, String sub, List<String> legacyClientIds) {
		if (azp != null && legacyClientIds != null && legacyClientIds.contains(azp)) {
			if (isBlank(preferredUsername)) {
				throw new NotAuthorizedException("Bearer");
			}
			return new Identity(preferredUsername, SOURCE_GOOGLE);
		}
		if (isBlank(sub)) {
			throw new NotAuthorizedException("Bearer");
		}
		return new Identity(sub, SOURCE_KEYCLOAK);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * The stored identity of a caller: {@code user_auth_data.authid} and {@code authsource}.
	 */
	public static final class Identity {
		private final String authid;
		private final String authsource;

		Identity(String authid, String authsource) {
			this.authid = authid;
			this.authsource = authsource;
		}

		public String authid() {
			return authid;
		}

		public String authsource() {
			return authsource;
		}
	}
}
