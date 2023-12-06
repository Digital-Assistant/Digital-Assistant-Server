package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.models.SequenceVotes;
import com.nistapp.uda.index.repository.SequenceListDAO;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Path("/votes")
public class Votes {

	private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@Inject
	SequenceListDAO sequenceListDAO;

	/**
	 * Get sequence votes by sequence ID
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SequenceVotes> getSequenceVotes(@PathParam("id") long id) {
		TypedQuery<SequenceVotes> query = em.createQuery("select sqv from SequenceVotes sqv where sqv.sequenceid=:id", SequenceVotes.class);
		query.setParameter("id", id);
		return query.getResultList();
	}

	/**
	 * Get sequence vote record by sequence ID and User Session ID
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id}/{userSessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public SequenceVotes getSequenceVotes(@PathParam("id") Integer id, @PathParam("userSessionId") String userSessionId) {
		try {
			SequenceVotes dbSequenceVotes = sequenceVotesDAO.findBySequenceIdUserSessionId(id, userSessionId);
			return dbSequenceVotes;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Add/Update user vote
	 * @param sequenceVotes
	 * @return
	 */
	@POST
	@Path("/addVote")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public SequenceList addSequenceVote(SequenceVotes sequenceVotes) {

		try {
			SequenceVotes dbSequenceVotes = sequenceVotesDAO.findBySequenceIdUserSessionId((Integer) sequenceVotes.getSequenceid(), sequenceVotes.getUsersessionid());
			if (dbSequenceVotes != null) {
				dbSequenceVotes.setUpvote((Integer) sequenceVotes.getUpvote());
				dbSequenceVotes.setDownvote((Integer) sequenceVotes.getDownvote());
				sequenceVotes = dbSequenceVotes;
			} else {
				System.out.println("sequence vote not found");
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		sequenceVotes.persist();
		SequenceList sequenceList = sequenceListDAO.findById(sequenceVotes.getSequenceid());
		em.persist(sequenceList);
		return sequenceList;
	}

	@DELETE
	@Path("/deleteVote/{id}/{userSessionId}")
	@Transactional
	public void deleteSequenceVote(@PathParam("id") long id, @PathParam("userSessionId") String userSessionId) {
		SequenceVotes dbSequenceVotes = sequenceVotesDAO.findbyusersessionid(userSessionId);
		if (dbSequenceVotes != null) {
			dbSequenceVotes.delete();
		}
	}
}
