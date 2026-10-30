package com.nistapp.uda.index.users;

import com.nistapp.uda.index.models.UserAuthData;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
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
 *     <li>a token of a public PKCE client ({@code azp} in {@code udan.auth.pkce-client-ids}, by default
 *     {@code udan-extension}) maps to ({@code sub}, {@code keycloak}). This rule is checked first, so it holds
 *     even when the same client id is also listed as legacy ({@link AuthClients});</li>
 *     <li>any other token maps to ({@code sub}, {@code keycloak}) too.</li>
 * </ul>
 * There is no linking: a Keycloak user is a new user even when an older google row has the same email hash (owner
 * decisions Q2/Q10, start fresh). Their owner key is the numeric row id that {@code checkauthid} returns as
 * {@code id} ({@link #ownerKey()}).
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

	@Inject
	AuthClients authClients;

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
					authClients.legacyClientIds(), authClients.pkceClientIds());
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
	 * The value stored as owner ({@code usersessionid}) on the caller's new recordings, votes, click nodes and
	 * click tracks.
	 * <ul>
	 *     <li>legacy (google) callers: their {@code authid}, which is what the extension has always sent and what
	 *     existing rows hold, so the Client's "is this mine" check ({@code usersessionid === authData.id}) keeps
	 *     working;</li>
	 *     <li>everyone else, and any authid made only of digits: the caller's numeric {@code user_auth_data.id}.</li>
	 * </ul>
	 * An all-digit owner value therefore always means a {@code user_auth_data.id} (the form migration V1.0.3 wrote),
	 * so a token whose username is a number can never pass as the user with that row id.
	 */
	public String ownerKey() {
		Identity id = identity();
		if (SOURCE_GOOGLE.equals(id.authsource()) && !isDigits(id.authid())) {
			return id.authid();
		}
		return String.valueOf(authData().getId());
	}

	/**
	 * Every stored owner value that means the caller: the {@link #ownerKey()} plus, for a legacy caller, the
	 * numeric row id that rows written before the switch to authids may hold. Used to filter searches.
	 */
	public List<String> ownerKeys() {
		String rowId = String.valueOf(authData().getId());
		String key = ownerKey();
		return key.equals(rowId) ? List.of(rowId) : List.of(key, rowId);
	}

	/**
	 * Whether a stored owner value ({@code usersessionid}) is the caller. A blank value is nobody's.
	 */
	public boolean owns(String storedOwner) {
		if (isBlank(storedOwner)) {
			return false;
		}
		if (isDigits(storedOwner)) {
			return storedOwner.equals(String.valueOf(authData().getId()));
		}
		Identity id = identity();
		return SOURCE_GOOGLE.equals(id.authsource()) && storedOwner.equals(id.authid());
	}

	/**
	 * @throws ForbiddenException when the stored owner value is not the caller
	 */
	public void requireOwner(String storedOwner) {
		if (!owns(storedOwner)) {
			throw new ForbiddenException();
		}
	}

	static boolean isDigits(String value) {
		return value != null && !value.isEmpty() && value.chars().allMatch(c -> c >= '0' && c <= '9');
	}

	/**
	 * Pure token-to-identity mapping, kept static so it can be unit tested without a token.
	 *
	 * @throws NotAuthorizedException when the claim the mapping needs is missing (fail closed)
	 */
	static Identity map(String azp, String preferredUsername, String sub, List<String> legacyClientIds,
			List<String> pkceClientIds) {
		boolean pkce = azp != null && pkceClientIds != null && pkceClientIds.contains(azp);
		if (!pkce && azp != null && legacyClientIds != null && legacyClientIds.contains(azp)) {
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
