package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.UserAuthData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UserAuthDataDAO  implements PanacheRepositoryBase<UserAuthData, Long> {
	/**
	 * All rows for (authid, authsource), oldest first. The pair has no unique constraint, so there may be more
	 * than one.
	 */
	public List<UserAuthData> findAllByAuthid(String authid, String authsource) {
		return find("authid=:authid and authsource=:authsource", Sort.ascending("id"),
				Parameters.with("authid", authid).and("authsource", authsource)).list();
	}
}
