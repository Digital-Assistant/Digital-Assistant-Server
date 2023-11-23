package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.UserSessionData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSessionDataDAO implements PanacheRepositoryBase<UserSessionData, Long> {
	public UserSessionData findbyusersessionid(String sessionid){
		return find("usersessionid",sessionid).singleResult();
	}
}
