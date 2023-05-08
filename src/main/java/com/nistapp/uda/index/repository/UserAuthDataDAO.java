package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.UserAuthData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserAuthDataDAO  implements PanacheRepositoryBase<UserAuthData, Long> {
	public UserAuthData findbyauthid(String authid, String authsource){
		return find("authid=:authid and authsource=:authsource", Parameters.with("authid",authid).and("authsource",authsource)).singleResult();
	}
}
