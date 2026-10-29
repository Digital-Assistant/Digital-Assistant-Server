package com.nistapp.uda.index.users;

import com.nistapp.uda.index.models.UserAuthData;
import com.nistapp.uda.index.models.UserSessionData;
import com.nistapp.uda.index.repository.UserAuthDataDAO;
import com.nistapp.uda.index.repository.UserSessionDataDAO;
import com.nistapp.uda.index.utils.LoggerUtil;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Finds or creates user rows and session bindings for an identity that was already taken from the token
 * ({@link CurrentUser}). Nothing here reads identity from a request body.
 */
@ApplicationScoped
public class UserAccountService {

	private static final Logger logger = LoggerUtil.getLogger(UserAccountService.class);

	@Inject
	UserAuthDataDAO userAuthDataDAO;

	@Inject
	UserSessionDataDAO userSessionDataDAO;

	/**
	 * The row for (authid, authsource), created when there is none. There is no unique constraint on the pair,
	 * so older data can hold duplicates: the oldest row wins and the duplicates are logged (ids only).
	 *
	 * @param emailid stored on a new row, or on an existing row that has none; may be null
	 */
	@Transactional
	public UserAuthData findOrCreate(String authid, String authsource, String emailid) {
		List<UserAuthData> rows = userAuthDataDAO.findAllByAuthid(authid, authsource);
		if (rows.size() > 1) {
			LoggerUtil.warn(logger, "user_auth_data has " + rows.size() + " rows for one (authid, authsource);"
					+ " using the oldest, id " + rows.get(0).getId());
		}
		if (!rows.isEmpty()) {
			UserAuthData row = rows.get(0);
			if (isBlank(row.getEmailid()) && !isBlank(emailid)) {
				row.setEmailid(emailid);
			}
			return row;
		}
		UserAuthData row = new UserAuthData();
		row.setAuthid(authid);
		row.setAuthsource(authsource);
		row.setEmailid(isBlank(emailid) ? null : emailid);
		userAuthDataDAO.persist(row);
		return row;
	}

	/**
	 * Binds a session id to the given user. A binding of the same session id to another user is never returned
	 * or changed: the caller gets its own, fresh binding (owner decision Q12: no 403, old builds keep working).
	 */
	@Transactional
	public UserSessionData bindSession(String usersessionid, long userauthid) {
		List<UserSessionData> own = userSessionDataDAO.findAllBySessionAndUser(usersessionid, userauthid);
		if (!own.isEmpty()) {
			return own.get(0);
		}
		UserSessionData row = new UserSessionData();
		row.setUsersessionid(usersessionid);
		row.setUserauthid(userauthid);
		userSessionDataDAO.persist(row);
		return row;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
