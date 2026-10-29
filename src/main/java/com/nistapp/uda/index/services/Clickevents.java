package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.*;
import com.nistapp.uda.index.repository.SequenceListDAO;
import com.nistapp.uda.index.repository.UserclicknodesRepository;
import com.nistapp.uda.index.users.CurrentUser;
import com.nistapp.uda.index.utils.SequenceListStatus;
import io.quarkus.security.Authenticated;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.*;

@Path("/clickevents")
@Authenticated
public class Clickevents {

    private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

    @Inject
    EntityManager em;

    @Inject
    SequenceListDAO sequenceListDAO;

    @Inject
    UserclicknodesRepository userclicknodesRepository;

    @Inject
    SequenceListStatus sequenceListStatus;

    @Inject
    CurrentUser currentUser;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<JavascriptEvents> listalldata() {
        TypedQuery<JavascriptEvents> query = em.createQuery("select e from JavascriptEvents e", JavascriptEvents.class);
        return query.getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public JavascriptEvents indexeddata(@Valid JavascriptEvents javascriptevents) {
        em.persist(javascriptevents);
        return javascriptevents;
    }

    @GET
    @Path("/fetchbyurl")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<JavascriptEvents> listdatabyurl(@QueryParam("url") @DefaultValue("NA") String url) {
        TypedQuery<JavascriptEvents> query = em
                .createQuery("select  e from JavascriptEvents e where e.urlpath=:url", JavascriptEvents.class)
                .setParameter("url", url);
        return query.getResultList();
    }

    @GET
    @Path("/fetchrecorddata")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<Userclicknodes> getuserrecordeddata(@QueryParam("start") long starttimestamp,
            @QueryParam("end") long endtimestamp, @QueryParam("sessionid") String sessionid,
            @QueryParam("domain") String domain) {
        TypedQuery<Userclicknodes> query = em.createQuery(
                "select u from Userclicknodes u where u.sessionid=:sessionid and u.domain=:domain and u.createdat between :startdate and :enddate",
                Userclicknodes.class);
        query.setParameter("sessionid", sessionid).setParameter("domain", domain)
                .setParameter("startdate", starttimestamp).setParameter("enddate", endtimestamp);
        return query.getResultList();
    }

    @GET
    @Path("/fetchtimestamp")
    @Produces(MediaType.TEXT_PLAIN)
    public long gettimestamp() {
        Date date = new Date();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        return time;
    }

    /**
     * Saves the clicked node data for a user. The owner is the caller from the token; a body
     * {@code usersessionid} is ignored.
     *
     * @param userClickNodes the clicked node data to be saved
     * @return the saved clicked node data
     */
    @POST
    @Path("/clickednode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Userclicknodes Userclickednode(@Valid Userclicknodes userClickNodes) {
        userClickNodes.setId(null);
        userClickNodes.setSequenceList(null);
        userClickNodes.setOwner(currentUser.ownerKey());
        em.persist(userClickNodes);
        em.flush();
        return userClickNodes;
    }

    /**
     * Updates the name and object data of a clicked node. Only the node's owner may change it.
     *
     * @param userClickNodes the node id and the new values; {@code sessionid} is only used for nodes recorded before
     *                       nodes had an owner (see {@link #requireClickNodeOwner})
     * @return the updated Userclicknodes object
     * @throws BadRequestException when the id is missing
     * @throws NotFoundException   when no node has that id
     * @throws ForbiddenException  when the caller does not own the node
     */
    @POST
    @Path("/updateclickednode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Userclicknodes UpdateUserclickedNode(Userclicknodes userClickNodes) {
        if (userClickNodes == null || userClickNodes.getId() == null) {
            throw new BadRequestException("id is required");
        }
        Userclicknodes clicknode = userclicknodesRepository.findById(userClickNodes.getId());
        if (clicknode == null) {
            throw new NotFoundException();
        }
        requireClickNodeOwner(clicknode, userClickNodes.getSessionid());
        clicknode.setClickednodename(userClickNodes.getClickednodename());
        clicknode.setObjectdata(userClickNodes.getObjectdata());
        em.persist(clicknode);
        return clicknode;
    }

    /**
     * A node is the caller's when its owner is the caller. Nodes recorded before V1.0.9 have no owner: they belong
     * to the owner of the recording they are in, and a node that is in no recording yet may be changed only from the
     * recording session that created it (the check this endpoint always made).
     */
    private void requireClickNodeOwner(Userclicknodes node, String sentSessionId) {
        if (node.getOwner() != null) {
            currentUser.requireOwner(node.getOwner());
        } else if (node.getSequenceList() != null) {
            currentUser.requireOwner(node.getSequenceList().getOwner());
        } else if (sentSessionId == null || !sentSessionId.equals(node.getSessionid())) {
            throw new ForbiddenException();
        }
    }

    @POST
    @Path("/recordsequencedata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList indexeddata(@Valid SequenceList sequenceList) {
        // Check every node before anything is written: a recording may only take the caller's own nodes, never
        // move nodes out of someone else's recording.
        Set<Userclicknodes> nodes = new HashSet<>();
        if (sequenceList.getUserclicknodesSet() != null) {
            for (Userclicknodes userclicknodes : sequenceList.getUserclicknodesSet()) {
                Userclicknodes u2 = userclicknodes == null || userclicknodes.getId() == null ? null
                        : em.find(Userclicknodes.class, userclicknodes.getId());
                if (u2 == null) {
                    throw new BadRequestException("unknown click node");
                }
                if (u2.getOwner() != null) {
                    currentUser.requireOwner(u2.getOwner());
                } else if (u2.getSequenceList() != null) {
                    currentUser.requireOwner(u2.getSequenceList().getOwner());
                }
                nodes.add(u2);
            }
        }
        String owner = currentUser.ownerKey();

        SequenceList s2 = new SequenceList();
        s2.setDomain(sequenceList.getDomain());
        s2.setUsersessionid(owner);
        s2.setName(sequenceList.getName());
        s2.setUserclicknodelist(sequenceList.getUserclicknodelist());
        s2.setIsValid(sequenceList.getIsValid());
        s2.setIsIgnored(sequenceList.getIsIgnored());
        s2.setAdditionalParams(sequenceList.getAdditionalParams());

        Integer draftStatusId = sequenceListStatus.getDraftStatusId();
        Integer publishedStatusId = sequenceListStatus.getPublishedStatusId();

        Map<String, Object> additionalParams = sequenceList.getAdditionalParams();
        if (additionalParams != null) {
            JSONObject jsonObject = new JSONObject(additionalParams);
            if (jsonObject.containsKey("enableStatus") && jsonObject.get("enableStatus").toString().equals("1")
                    && !jsonObject.containsKey("status")) {
                // Convert JSONObject back to Map before setting
                additionalParams.put("status", draftStatusId.toString());
                s2.setAdditionalParams(additionalParams);
            }
        }

        em.persist(s2);

        for (Userclicknodes u2 : nodes) {
            u2.setSequenceList(s2);
        }
        s2.setUserclicknodesSet(nodes);

        Set<SequenceVotes> votes = new HashSet<>();
        SequenceVotes sequenceVotes = new SequenceVotes();
        sequenceVotes.setSequenceid(s2.getId());
        sequenceVotes.setUsersessionid(owner);
        sequenceVotes.setDownvote(0);
        sequenceVotes.setUpvote(0);
        sequenceVotes.persist();

        votes.add(sequenceVotes);
        s2.setSequenceVotes(votes);

        em.persist(s2);
        return s2;
    }

    /**
     * Replaces the additional parameters of a recording (old path; the SDK now uses {@code /sequence/update}).
     * Only the recording's owner may change it; a body {@code usersessionid} is ignored.
     *
     * @param sequenceList the recording id and the new additional parameters
     * @return the updated sequence list object
     * @throws BadRequestException when the id is missing
     * @throws NotFoundException   when no recording has that id
     * @throws ForbiddenException  when the caller does not own the recording
     */
    @POST
    @Path("/updatesequencedata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList updateSequenceData(SequenceList sequenceList) {
        SequenceList updateSequenceList = findOwnedSequence(sequenceList);
        logger.info("Updating additionalParams of sequence {}", updateSequenceList.getId());
        updateSequenceList.setAdditionalParams(sequenceList.getAdditionalParams());
        em.persist(updateSequenceList);
        return updateSequenceList;
    }

    /**
     * Loads the recording named by the body's id and checks that the caller owns it.
     *
     * @throws BadRequestException when the id is missing
     * @throws NotFoundException   when no recording has that id
     * @throws ForbiddenException  when the caller does not own it
     */
    private SequenceList findOwnedSequence(SequenceList sent) {
        if (sent == null || sent.getId() == null) {
            throw new BadRequestException("id is required");
        }
        SequenceList dbresult = sequenceListDAO.findById(sent.getId());
        if (dbresult == null) {
            throw new NotFoundException();
        }
        currentUser.requireOwner(dbresult.getOwner());
        return dbresult;
    }

    /*
     * @Transactional
     * void onStart(@Observes StartupEvent event) throws InterruptedException {
     * logger.info(ConfigProvider.getConfig().getConfigSources().toString());
     * Long value = em.
     * createQuery("SELECT COUNT(s.id) FROM SequenceList s where s.deleted=0 and s.isValid=1 and s.isIgnored=0"
     * , Long.class).getSingleResult();
     * if (value != null && value != 0) {
     * Search.session(em).massIndexer(SequenceList.class).startAndWait();
     * }
     * }
     */

    @POST
    @Path("sequence/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    /**
     * Marks a recording deleted. Only the recording's owner may delete it; a body {@code usersessionid} is ignored.
     *
     * @throws BadRequestException when the id is missing
     * @throws NotFoundException   when no recording has that id
     * @throws ForbiddenException  when the caller does not own the recording
     */
    public SequenceList deletesequence(SequenceList sequenceList) {
        SequenceList dbresult = findOwnedSequence(sequenceList);
        dbresult.setDeleted(1);
        em.persist(dbresult);
        return dbresult;
    }

    @GET
    @Path("sequence/votes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SequenceVotes> getsequencevotes(@PathParam("id") long id) {
        TypedQuery<SequenceVotes> query = em.createQuery("select sqv from SequenceVotes sqv where sqv.sequenceid=:id",
                SequenceVotes.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @PUT
    @Path("userclick")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void addUserClick(@Valid ClickTrack clickTrack) {
        // The user is the caller from the token; a body usersessionid is ignored.
        clickTrack.setUsersessionid(currentUser.ownerKey());
        clickTrack.persist();
    }

    /**
     * Retrieves a list of suggested Userclicknodes based on the specified domain.
     *
     * @param domain the domain to filter the Userclicknodes by
     * @return a list of suggested Userclicknodes
     */
    @GET
    @Path("/suggested")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Userclicknodes> suggestSequenceList(@QueryParam("domain") String domain) {
        List<Long> nodeids = getRandomNumberList();
        List<Userclicknodes> userclicknodes = new ArrayList<>();

        TypedQuery<Userclicknodes> query = em.createQuery(
                "select u from Userclicknodes u where u.id in :id and u.domain=:domain", Userclicknodes.class);
        query.setParameter("domain", domain);
        query.setParameter("id", nodeids);
        userclicknodes = query.getResultList();

        return userclicknodes;
    }

    /**
     * Generates a random list of integers.
     *
     * @return intList A list of randomly generated integers.
     */
    public static List<Long> getRandomNumberList() {

        Random rand = new Random();

        Integer generateLength = rand.nextInt((10 - 3) + 1) + 3;

        List<Long> intList = new ArrayList<>();

        for (int i = 0; i < generateLength; i++) {
            intList.add((long) rand.nextInt(100));
        }

        return intList;
    }
}
