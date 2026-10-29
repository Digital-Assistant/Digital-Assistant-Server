package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceVotes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SequenceVotesDAO implements PanacheRepositoryBase<SequenceVotes, Long> {

	public List<SequenceVotes> findBySequenceId(Long sequenceId){
		return find("sequenceid",sequenceId).list();
	}

	public List<SequenceVotes> findAllBySequenceIdUserSessionId(Long sequenceId, String userSessionId){
		return find("sequenceid=:sequenceid and usersessionid=:usersessionid", Parameters.with("sequenceid",sequenceId).and("usersessionid",userSessionId)).list();
	}

	/**
	 * A voter's votes on one recording, oldest first.
	 *
	 * @param owners every stored owner value that means the voter ({@code CurrentUser.ownerKeys()})
	 */
	public List<SequenceVotes> findAllBySequenceIdAndOwners(Long sequenceId, List<String> owners){
		return find("sequenceid=:sequenceid and usersessionid in :owners order by id",
				Parameters.with("sequenceid",sequenceId).and("owners",owners)).list();
	}
}
