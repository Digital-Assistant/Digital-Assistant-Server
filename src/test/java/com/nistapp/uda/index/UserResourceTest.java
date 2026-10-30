package com.nistapp.uda.index;

import com.nistapp.uda.index.models.UserAuthData;
import com.nistapp.uda.index.repository.UserAuthDataDAO;
import com.nistapp.uda.index.repository.UserSessionDataDAO;
import com.nistapp.uda.index.users.UserAccountService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract tests for the /user endpoints: the session key, and the user/session binding that takes the caller
 * from the token (CurrentUser), never from the body. Users A and B carry tokens of the legacy confidential client
 * ({@code azp = backend-service}), so they map to (preferred_username, google) like today's extension users.
 */
@QuarkusTest
public class UserResourceTest {

    static final String USER_A = "pk02-test-user-a";
    static final String USER_B = "pk02-test-user-b";
    static final String PKCE_SUB = "pk02-test-sub-0001";
    static final String PK10_SUB_REPEAT = "pk10-test-sub-repeat";
    static final String PK10_SUB_NOLINK = "pk10-test-sub-nolink";
    static final String PK10_GOOGLE_USER = "pk10-test-google-user";
    static final String PK10_EMAIL = "pk10-user@example.test";

    @Inject
    UserAccountService userAccountService;

    @Inject
    UserAuthDataDAO userAuthDataDAO;

    @Inject
    UserSessionDataDAO userSessionDataDAO;

    @Test
    void testGetSessionKeyReturnsCookie() {
        given()
                .when().get("/api/user/getsessionkey")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .header("Set-Cookie", containsString("nist-voice-usersessionid"));
    }

    @Test
    void testGetSessionKeyWithExistingCookieReturnsSameValue() {
        String sessionKey = "existing-session-123";
        given()
                .cookie("nist-voice-usersessionid", sessionKey)
                .when().get("/api/user/getsessionkey")
                .then()
                .statusCode(200)
                .body(equalTo(sessionKey));
    }

    // --- anonymous callers (old clients call checkauthid/checkusersession before they hold a token)

    @Test
    void anonymousCheckAuthIdGetsPlaceholderAndReadsNothing() {
        long before = userAuthDataDAO.count();
        String victim = userAccountService.findOrCreate(USER_B, "google", null).getAuthid();
        long withVictim = userAuthDataDAO.count();
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"" + victim + "\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(0))
                .body("emailid", nullValue())
                .body("authid", equalTo(victim));
        assertEquals(withVictim, userAuthDataDAO.count(), "anonymous checkauthid must not store a row");
        assertTrue(withVictim - before <= 1);
    }

    @Test
    void anonymousCheckAuthIdWithUnknownIdCreatesNothing() {
        long before = userAuthDataDAO.count();
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"pk02-junk-" + UUID.randomUUID() + "\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("id", equalTo(0));
        assertEquals(before, userAuthDataDAO.count());
    }

    @Test
    void anonymousCheckAuthIdRejectsBlankAuthId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(400);
    }

    @Test
    void anonymousCheckUserSessionBindsNothing() {
        long victimId = userAccountService.findOrCreate(USER_B, "google", null).getId();
        String session = "pk02-anon-" + UUID.randomUUID();
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"" + session + "\",\"userauthid\":" + victimId + "}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userauthid", equalTo(0))
                .body("id", equalTo(0));
        assertEquals(0, userSessionDataDAO.count("usersessionid", session));
    }

    // --- authenticated callers

    @Test
    @TestSecurity(user = USER_A)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
    void checkAuthIdReturnsCallersOwnRowAndIgnoresBodyIdentity() {
        long victimId = userAccountService.findOrCreate(USER_B, "google", null).getId();
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"" + USER_B + "\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("authid", equalTo(USER_A))
                .body("authsource", equalTo("google"))
                .body("id", not(equalTo((int) victimId)));
    }

    @Test
    @TestSecurity(user = USER_A)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
    void checkAuthIdKeepsTheOldBodyContract() {
        // The body is still validated as before (authid must not be blank), even though it is ignored.
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = USER_A)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
    void checkUserSessionBindsToCallerNotToBodyUser() {
        long ownId = userAccountService.findOrCreate(USER_A, "google", null).getId();
        long victimId = userAccountService.findOrCreate(USER_B, "google", null).getId();
        String session = "pk02-a-" + UUID.randomUUID();
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"" + session + "\",\"userauthid\":" + victimId + "}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(200)
                .body("userauthid", equalTo((int) ownId))
                .body("usersessionid", equalTo(session));
        assertEquals(0, userSessionDataDAO.count("usersessionid = ?1 and userauthid = ?2", session, victimId));
        assertEquals(1, userSessionDataDAO.count("usersessionid = ?1 and userauthid = ?2", session, ownId));
    }

    @Test
    @TestSecurity(user = USER_A)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
    void sessionBoundToAnotherUserIsNotTakenOverOrReturned() {
        long ownId = userAccountService.findOrCreate(USER_A, "google", null).getId();
        long victimId = userAccountService.findOrCreate(USER_B, "google", null).getId();
        String session = "pk02-shared-" + UUID.randomUUID();
        long victimBinding = userAccountService.bindSession(session, victimId).getId();
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"" + session + "\",\"userauthid\":" + victimId + "}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(200)
                .body("userauthid", equalTo((int) ownId))
                .body("id", not(equalTo((int) victimBinding)));
        assertEquals(1, userSessionDataDAO.count("usersessionid = ?1 and userauthid = ?2", session, victimId),
                "the other user's binding stays as it was");
    }

    @Test
    @TestSecurity(user = USER_A)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
    void checkUserSessionRejectsBlankSessionId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"\",\"userauthid\":1}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "someone")
    @OidcSecurity(claims = {@Claim(key = "azp", value = "udan-extension"), @Claim(key = "sub", value = PKCE_SUB)})
    void tokenOfAnotherClientMapsToSubAndKeycloak() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"" + USER_B + "\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("authid", equalTo(PKCE_SUB))
                .body("authsource", equalTo("keycloak"));
    }

    // --- PKCE tokens (public client udan-extension, track authPKCE PK10): (sub, keycloak), no linking

    @Test
    @TestSecurity(user = "pk10-someone")
    @OidcSecurity(claims = {@Claim(key = "azp", value = "udan-extension"),
            @Claim(key = "sub", value = PK10_SUB_REPEAT)})
    void pkceCheckAuthIdIsIdempotent() {
        int first = given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"anything\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("authid", equalTo(PK10_SUB_REPEAT))
                .body("authsource", equalTo("keycloak"))
                .body("id", greaterThan(0))
                .extract().path("id");
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"something-else\",\"authsource\":\"keycloak\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("id", equalTo(first));
        assertEquals(1, userAuthDataDAO.count("authid = ?1 and authsource = ?2", PK10_SUB_REPEAT, "keycloak"),
                "repeat calls (and repeat test runs) keep one row per (sub, keycloak)");
    }

    @Test
    @TestSecurity(user = PK10_GOOGLE_USER)
    @OidcSecurity(claims = {@Claim(key = "azp", value = "udan-extension"), @Claim(key = "sub", value = PK10_SUB_NOLINK),
            @Claim(key = "email", value = PK10_EMAIL), @Claim(key = "email_verified", value = "true")})
    void pkceUserIsNotLinkedToAGoogleUserWithTheSameEmailOrUsername() throws Exception {
        // Owner decisions Q2/Q10: start fresh. Same verified email hash and a username equal to the google authid
        // still give a new keycloak row; the google row keeps its identity.
        String emailHash = sha512(PK10_EMAIL);
        UserAuthData google = userAccountService.findOrCreate(PK10_GOOGLE_USER, "google", emailHash);
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"" + PK10_GOOGLE_USER + "\",\"authsource\":\"google\",\"emailid\":\"" + emailHash
                        + "\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(200)
                .body("authid", equalTo(PK10_SUB_NOLINK))
                .body("authsource", equalTo("keycloak"))
                .body("id", not(equalTo((int) google.getId())));
        UserAuthData after = userAccountService.findOrCreate(PK10_GOOGLE_USER, "google", null);
        assertEquals(google.getId(), after.getId());
        assertEquals(PK10_GOOGLE_USER, after.getAuthid());
        assertEquals("google", after.getAuthsource());
    }

    @Test
    @TestSecurity(user = "pk10-someone")
    @OidcSecurity(claims = {@Claim(key = "azp", value = "udan-extension"),
            @Claim(key = "sub", value = PK10_SUB_REPEAT)})
    void pkceCheckUserSessionBindsToTheKeycloakRow() {
        long ownId = userAccountService.findOrCreate(PK10_SUB_REPEAT, "keycloak", null).getId();
        String session = "pk10-" + UUID.randomUUID();
        given()
                .contentType(ContentType.JSON)
                .body("{\"usersessionid\":\"" + session + "\",\"userauthid\":1}")
                .when().post("/api/user/checkusersession")
                .then()
                .statusCode(200)
                .body("userauthid", equalTo((int) ownId))
                .body("usersessionid", equalTo(session));
    }

    @Test
    @TestSecurity(user = "plain-user")
    void tokenWithoutClaimsIsRefused() {
        // @TestSecurity without @OidcSecurity gives a principal that is not a JWT: no usable identity.
        given()
                .contentType(ContentType.JSON)
                .body("{\"authid\":\"x\",\"authsource\":\"google\"}")
                .when().post("/api/user/checkauthid")
                .then()
                .statusCode(401);
    }

    @Test
    void duplicateRowsResolveToTheOldest() {
        String authid = "pk02-dup-" + UUID.randomUUID();
        long first = insertRow(authid);
        insertRow(authid);
        assertEquals(first, userAccountService.findOrCreate(authid, "google", null).getId());
    }

    static String sha512(String email) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-512").digest(email.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    long insertRow(String authid) {
        return QuarkusTransaction.requiringNew().call(() -> {
            UserAuthData row = new UserAuthData();
            row.setAuthid(authid);
            row.setAuthsource("google");
            userAuthDataDAO.persist(row);
            return row.getId();
        });
    }
}
