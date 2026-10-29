package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.UserSessionData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UserSessionDataDAO implements PanacheRepositoryBase<UserSessionData, Long> {
	/**
	 * The bindings of one session id to one user, oldest first.
	 */
	public List<UserSessionData> findAllBySessionAndUser(String usersessionid, long userauthid) {
		return find("usersessionid=:usersessionid and userauthid=:userauthid", Sort.ascending("id"),
				Parameters.with("usersessionid", usersessionid).and("userauthid", userauthid)).list();
	}
}
