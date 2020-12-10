package com.nistapp.voice.index.repository;

import com.nistapp.voice.index.models.Userclicknodes;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserclicknodesRepository implements PanacheRepository<Userclicknodes> {
	public Userclicknodes findbynodeid(String sessionid, Integer nodeId){
		return find("sessionid=:sessionId and id=:nodeId", Parameters.with("sessionId",sessionid).and("nodeId",nodeId)).singleResult();
	}
}
