package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceVotes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SequenceVotesDAO implements PanacheRepositoryBase<SequenceVotes, Long> {

	public SequenceVotes findbyusersessionid(String usersessionid){
		return find("usersessionid",usersessionid).singleResult();
	}

	public List<SequenceVotes> findBySequenceId(Long sequenceId){
		return find("sequenceid",sequenceId).list();
	}

	public List<SequenceVotes> findAllBySequenceIdUserSessionId(Long sequenceId, String userSessionId){
		return find("sequenceid=:sequenceid and usersessionid=:usersessionid", Parameters.with("sequenceid",sequenceId).and("usersessionid",userSessionId)).list();
	}

	public SequenceVotes findBySequenceIdUserSessionId(Long sequenceId, String userSessionId){
		return find("sequenceid=:sequenceid and usersessionid=:usersessionid", Parameters.with("sequenceid",sequenceId).and("usersessionid",userSessionId)).singleResult();
	}
}
