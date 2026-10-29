package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.models.SequenceVotes;
import com.nistapp.uda.index.repository.SequenceListDAO;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
import com.nistapp.uda.index.users.CurrentUser;
import io.quarkus.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/votes")
@Authenticated
public class Votes {

	private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@Inject
	SequenceListDAO sequenceListDAO;

	@Inject
	CurrentUser currentUser;

	/**
	 * Get sequence votes by sequence ID
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SequenceVotes> getSequenceVotes(@PathParam("id") long id) {
		TypedQuery<SequenceVotes> query = em.createQuery("select sqv from SequenceVotes sqv where sqv.sequenceid=:id",
				SequenceVotes.class);
		query.setParameter("id", id);
		return query.getResultList();
	}

	/**
	 * The caller's own vote on a recording. The path's user value is ignored (kept so old clients work): the voter
	 * is the caller from the token.
	 *
	 * @param id            recording id
	 * @param userSessionId ignored
	 * @return the caller's vote, or nothing (204) when the caller has not voted
	 */
	@GET
	@Path("/{id}/{userSessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public SequenceVotes getSequenceVotes(@PathParam("id") Long id,
			@PathParam("userSessionId") String userSessionId) {
		List<SequenceVotes> own = sequenceVotesDAO.findAllBySequenceIdAndOwners(id, currentUser.ownerKeys());
		return own.isEmpty() ? null : own.get(0);
	}

	/**
	 * Adds or updates the caller's vote on a recording. A body {@code usersessionid} is ignored: the voter is the
	 * caller from the token.
	 *
	 * @param sequenceVotes {@code sequenceid}, {@code upvote}, {@code downvote}
	 * @return the recording with its votes
	 * @throws BadRequestException when the recording id is missing
	 * @throws NotFoundException   when no recording has that id
	 */
	@POST
	@Path("/addVote")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public SequenceList addSequenceVote(@Valid SequenceVotes sequenceVotes) {
		if (sequenceVotes == null || sequenceVotes.getSequenceid() == null) {
			throw new BadRequestException("sequenceid is required");
		}
		Long sequenceId = sequenceVotes.getSequenceid();
		// count, not findById: loading the recording now would cache its vote set before the vote below is written
		if (sequenceListDAO.count("id", sequenceId) == 0) {
			throw new NotFoundException();
		}
		List<SequenceVotes> own = sequenceVotesDAO.findAllBySequenceIdAndOwners(sequenceId, currentUser.ownerKeys());
		SequenceVotes vote;
		if (own.isEmpty()) {
			vote = new SequenceVotes();
			vote.setSequenceid(sequenceId);
			vote.setUsersessionid(currentUser.ownerKey());
		} else {
			vote = own.get(0);
		}
		vote.setUpvote(sequenceVotes.getUpvote());
		vote.setDownvote(sequenceVotes.getDownvote());
		vote.persist();
		SequenceList sequenceList = sequenceListDAO.findById(sequenceId);
		em.persist(sequenceList);
		return sequenceList;
	}

	/**
	 * Removes the caller's vote on recording {@code id}. The path's user value is ignored (kept so old clients
	 * work): only the caller's own vote on that recording is removed.
	 *
	 * @param id            recording id
	 * @param userSessionId ignored
	 * @throws NotFoundException when the caller has no vote on that recording
	 */
	@DELETE
	@Path("/deleteVote/{id}/{userSessionId}")
	@Transactional
	public void deleteSequenceVote(@PathParam("id") long id, @PathParam("userSessionId") String userSessionId) {
		List<SequenceVotes> own = sequenceVotesDAO.findAllBySequenceIdAndOwners(id, currentUser.ownerKeys());
		if (own.isEmpty()) {
			throw new NotFoundException();
		}
		own.forEach(SequenceVotes::delete);
	}
}
