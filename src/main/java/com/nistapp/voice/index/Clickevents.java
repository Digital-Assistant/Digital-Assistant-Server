package com.nistapp.voice.index;

import com.nistapp.voice.index.models.*;
import com.nistapp.voice.index.repository.SequenceVotesDAO;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

import java.text.SimpleDateFormat;

@Path("/clickevents")
public class Clickevents {

    private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

    @Inject
    EntityManager em;

    @Inject
    SequenceVotesDAO sequenceVotesDAO;

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
    public JavascriptEvents indexeddata(JavascriptEvents javascriptevents) {
        em.persist(javascriptevents);
        return javascriptevents;
    }

    @GET
    @Path("/fetchbyurl")
//	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<JavascriptEvents> listdatabyurl(@QueryParam("url") @DefaultValue("NA") String url) {
        TypedQuery<JavascriptEvents> query = em.createQuery("select  e from JavascriptEvents e where e.urlpath=:url", JavascriptEvents.class).setParameter("url", url);
        return query.getResultList();
    }

    @GET
    @Path("/fetchrecorddata")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<Userclicknodes> getuserrecordeddata(@QueryParam("start") long starttimestamp, @QueryParam("end") long endtimestamp, @QueryParam("sessionid") String sessionid, @QueryParam("domain") String domain) {
        TypedQuery<Userclicknodes> query = em.createQuery("select u from Userclicknodes u where u.sessionid=:sessionid and u.domain=:domain and u.createdat between :startdate and :enddate", Userclicknodes.class);
        query.setParameter("sessionid", sessionid).setParameter("domain", domain).setParameter("startdate", starttimestamp).setParameter("enddate", endtimestamp);
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

    @POST
    @Path("/recordsequencedata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList indexeddata(SequenceList sequenceList) {
        SequenceList s2 = new SequenceList();
        s2.setDomain(sequenceList.getDomain());
        s2.setUsersessionid(sequenceList.getUsersessionid());
        s2.setName(sequenceList.getName());
        s2.setUserclicknodelist(sequenceList.getUserclicknodelist());
        s2.setIsValid(sequenceList.getIsValid());
        s2.setIsIgnored(sequenceList.getIsIgnored());
        s2.setAdditionalParams(sequenceList.getAdditionalParams());
        em.persist(s2);
        List<Userclicknodes> list = new ArrayList<>();
        /*for (Userclicknodes userclicknodes : sequenceList.getUserclicknodesSet()) {
            Userclicknodes u2 = em.find(Userclicknodes.class, userclicknodes.getId());
            u2.setSequenceList(s2);
            list.add(u2);
        }*/
        s2.setUserclicknodesSet(list);
        em.persist(s2);
        return s2;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) throws InterruptedException {
        logger.info(ConfigProvider.getConfig().getConfigSources().toString());
        Long value = em.createQuery("SELECT COUNT(s.id) FROM SequenceList s where s.deleted=0 and s.isValid=1 and s.isIgnored=0", Long.class).getSingleResult();
        if (value != null && value != 0) {
            Search.session(em).massIndexer(SequenceList.class).startAndWait();
        }
    }

    @GET
    @Path("sequence/search")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<SequenceList> search(@QueryParam("query") String query, @QueryParam("domain") String domain, @QueryParam("size") Optional<Integer> size) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        logger.info("--------------------------------------------------------------------------------------------------");
        logger.info("Api invoked at:" + formatter.format(new Date()));
        final Function<SearchPredicateFactory, PredicateFinalStep> deletedFilter;
        deletedFilter = f -> f.match().field("deleted").matching(0);

        final Function<SearchPredicateFactory, PredicateFinalStep> validFilter;
        validFilter = f -> f.match().field("isValid").matching(1);

        final Function<SearchPredicateFactory, PredicateFinalStep> ignoreFilter;
        ignoreFilter = f -> f.match().field("isIgnored").matching(0);

        final Function<SearchPredicateFactory, PredicateFinalStep> domainFilter;
        final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
        if (domain != null && !domain.isEmpty()) {
            domainFilter = f -> f.match().field("domain").matching(domain);
        } else {
            domainFilter = null;
        }
        List<SequenceList> searchresults;
        logger.info("elastic search results start time:" + formatter.format(new Date()));
        if (query == null || query.isEmpty()) {
            queryFunction = domainFilter == null ?
                    SearchPredicateFactory::matchAll :
                    f -> f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f)).must(domainFilter.apply(f)).must(f.matchAll());
            searchresults = Search.session(em).search(SequenceList.class).where(queryFunction).sort(f -> f.field("createdat_sort").desc()).fetchHits(size.orElse(10));

        } else {
            queryFunction = domainFilter == null ?
                    f -> f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f)).must(f.simpleQueryString().fields("name", "userclicknodesSet.clickednodename")
                            .matching(query)) :
                    f -> f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f)).must(domainFilter.apply(f))
                            .must(f.simpleQueryString()
                                    .fields("name", "userclicknodesSet.clickednodename")
                                    .matching(query));
            searchresults = Search.session(em).search(SequenceList.class).where(queryFunction).fetchHits(size.orElse(10));
        }
        logger.info("elastic search results end time:" + formatter.format(new Date()));
        logger.info("--------------------------------------------------------------------------------------------------");
        return searchresults;

//        return Search.session(em).search(SequenceList.class).predicate(queryFunction).fetchAll().getHits();
    }

    @POST
    @Path("sequence/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList deletesequence(SequenceList sequenceList) {
        TypedQuery<SequenceList> query = em.createQuery("select sq from SequenceList sq where sq.id=:id", SequenceList.class);
        query.setParameter("id", sequenceList.getId());
        SequenceList dbresult = query.getSingleResult();
        if (dbresult == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        String dbuserid = dbresult.getUsersessionid();
        String sentuserid = sequenceList.getUsersessionid();
        if (dbuserid.equals(sentuserid)) {
            dbresult.setDeleted(1);
            em.persist(dbresult);
            return dbresult;
        } else {
            System.out.println("Not matched user sessionid");
            return null;
        }
    }


    @GET
    @Path("sequence/votes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SequenceVotes> getsequencevotes(@PathParam("id") long id) {
        TypedQuery<SequenceVotes> query = em.createQuery("select sqv from SequenceVotes sqv where sqv.sequenceid=:id", SequenceVotes.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @POST
    @Path("sequence/addvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceVotes addsequencevote(SequenceVotes sequenceVotes) {

        try {
            SequenceVotes dbsequenceVotes = sequenceVotesDAO.findbysequenceidusersessionid(sequenceVotes.getSequenceid(), sequenceVotes.getUsersessionid());
            if (dbsequenceVotes != null) {
                dbsequenceVotes.setUpvote(sequenceVotes.getUpvote());
                dbsequenceVotes.setDownvote(sequenceVotes.getDownvote());
                sequenceVotes = dbsequenceVotes;
            }
        } catch (Exception e) {

        }

        sequenceVotes.persist();
        return sequenceVotes;
    }

    @DELETE
    @Path("sequence/deletevote/{id}/{usersessionid}")
    @Transactional
    public void deletesequencevote(@PathParam("id") long id, @PathParam("usersessionid") String usersessionid) {
        SequenceVotes dbsequenceVotes = sequenceVotesDAO.findbyusersessionid(usersessionid);
        if (dbsequenceVotes != null) {
            dbsequenceVotes.delete();
        }
    }

    @PUT
    @Path("userclick")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void adduserclick(ClickTrack clickTrack) {
        clickTrack.persist();
    }

    @GET
    @Path("/suggested")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Userclicknodes> suggestSequenceList(@QueryParam("domain") String domain) {
        List<Integer> nodeids = this.getrandomnumber();
        List<Userclicknodes> userclicknodes = new ArrayList<>();

        TypedQuery<Userclicknodes> query = em.createQuery("select u from Userclicknodes u where u.id in :id and u.domain=:domain", Userclicknodes.class);
        query.setParameter("domain", domain);
        query.setParameter("id", nodeids);
        userclicknodes = query.getResultList();

        return userclicknodes;
    }

    public static List<Integer> getrandomnumber() {

        Random rand = new Random();

        Integer generatelength = rand.nextInt((10 - 3) + 1) + 3;

        List<Integer> intlist = new ArrayList<>();

        for (int i = 0; i < generatelength; i++) {
            intlist.add(rand.nextInt(100));
        }

        return intlist;
    }
}
