package com.nistapp.uda.index;

import com.nistapp.uda.index.models.UserAuthData;
import com.nistapp.uda.index.models.UserSessionData;
import com.nistapp.uda.index.users.CurrentUser;
import com.nistapp.uda.index.users.UserAccountService;
import com.nistapp.uda.index.utils.LoggerUtil;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;

/**
 * Session key and user binding for the browser extension and the SDK. Every method needs a token except
 * {@code getsessionkey} and the legacy anonymous answers described on {@code checkauthid}/{@code checkusersession}.
 */
@Path("/user")
@Authenticated
public class User {

    private static final Logger logger = LoggerUtil.getLogger(User.class);
    private static final int MAX_EMAILID = 2000;
    private static final int MAX_USERSESSIONID = 500;

    @Inject
    CurrentUser currentUser;

    @Inject
    UserAccountService userAccountService;

    /**
     * True until every client sends a token on checkauthid/checkusersession (PK15). See checkauthdata.
     */
    @ConfigProperty(name = "udan.auth.legacy-anonymous-login", defaultValue = "true")
    boolean legacyAnonymousLogin;

    private static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @GET
    @Path("/getsessionkey")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response getsessionkey(@CookieParam("nist-voice-usersessionid") Cookie sessioncookie) {
        if (sessioncookie == null) {
            String sessionkey = getAlphaNumericString(20);
            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 365);
            dt = c.getTime();
            NewCookie newCookie1 = new NewCookie("nist-voice-usersessionid", sessionkey, "/", "",
                    "Nistapp voice user session", 365 * 24 * 60 * 60, false);
            return Response.ok(sessionkey).cookie(newCookie1).build();
        } else {
            return Response.ok(sessioncookie.getValue()).build();
        }
    }

    /**
     * The caller's own user row. The identity comes from the bearer token ({@link CurrentUser}); {@code authid}
     * and {@code authsource} in the body are accepted for old clients and ignored (owner decision Q12). Only
     * {@code emailid} is kept, and only for a row that has none. The body is still validated as before
     * ({@code authid} must not be blank), so the old request contract is unchanged.
     * <p>
     * Anonymous calls: SDK 0.12.0 and the store build call this before they hold a token. While
     * {@code udan.auth.legacy-anonymous-login} is true they get a placeholder answer (id 0) that reads and stores
     * nothing; when it is false they get 401.
     */
    @POST
    @Path("/checkauthid")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserAuthData checkauthdata(@Valid UserAuthData postauthdata) {
        String emailid = postauthdata == null ? null : postauthdata.getEmailid();
        if (emailid != null && emailid.length() > MAX_EMAILID) {
            throw new BadRequestException("emailid must not exceed " + MAX_EMAILID + " characters");
        }
        if (currentUser.isAnonymous()) {
            requireLegacyAnonymousLogin();
            if (postauthdata == null || isBlank(postauthdata.getAuthid())) {
                throw new BadRequestException("Auth ID must not be blank");
            }
            LoggerUtil.debug(logger, "anonymous checkauthid answered with a placeholder (legacy client)");
            UserAuthData placeholder = new UserAuthData();
            placeholder.setAuthid(postauthdata.getAuthid());
            placeholder.setAuthsource(postauthdata.getAuthsource());
            return placeholder;
        }
        return currentUser.authData(emailid);
    }

    /**
     * Binds the session id in the body to the caller's own user row. {@code userauthid} in the body is ignored.
     * A session id already bound to another user is left alone and the caller gets a fresh binding of its own
     * (owner decision Q12). Anonymous calls are handled as in {@link #checkauthdata(UserAuthData)}.
     */
    @POST
    @Path("/checkusersession")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserSessionData checkusersession(@Valid UserSessionData postusersessiondata) {
        String usersessionid = postusersessiondata == null ? null : postusersessiondata.getUsersessionid();
        if (isBlank(usersessionid) || usersessionid.length() > MAX_USERSESSIONID) {
            throw new BadRequestException("usersessionid must be 1 to " + MAX_USERSESSIONID + " characters");
        }
        if (currentUser.isAnonymous()) {
            requireLegacyAnonymousLogin();
            LoggerUtil.debug(logger, "anonymous checkusersession answered with a placeholder (legacy client)");
            UserSessionData placeholder = new UserSessionData();
            placeholder.setUsersessionid(usersessionid);
            return placeholder;
        }
        return userAccountService.bindSession(usersessionid, currentUser.authData().getId());
    }

    private void requireLegacyAnonymousLogin() {
        if (!legacyAnonymousLogin) {
            throw new NotAuthorizedException("Bearer");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
