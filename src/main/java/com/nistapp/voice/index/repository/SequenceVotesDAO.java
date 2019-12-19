package com.nistapp.voice.index.repository;

import com.nistapp.voice.index.models.SequenceVotes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SequenceVotesDAO implements PanacheRepositoryBase<SequenceVotes, Long> {

	public SequenceVotes findbyusersessionid(String usersessionid){
		return find("usersessionid",usersessionid).singleResult();
	}

	public List<SequenceVotes> findbysequenceid(long sequenceid){
		return find("sequenceid",sequenceid).list();
	}

	public List<SequenceVotes> findallbysequenceidusersessionid(long sequenceid, String usersessionid){
		return find("sequenceid=:sequenceid and usersessionid=:usersessionid", Parameters.with("sequenceid",sequenceid).and("usersessionid",usersessionid)).list();
	}

	public SequenceVotes findbysequenceidusersessionid(long sequenceid, String usersessionid){
		return find("sequenceid=:sequenceid and usersessionid=:usersessionid", Parameters.with("sequenceid",sequenceid).and("usersessionid",usersessionid)).singleResult();
	}
}
