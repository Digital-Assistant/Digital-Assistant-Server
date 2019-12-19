package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;
import com.nistapp.voice.index.models.SequenceList;
import com.nistapp.voice.index.models.SequenceVotes;
import com.nistapp.voice.index.models.Userclicknodes;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Path("/clickevents")
public class Clickevents {

    private static Logger logger = LoggerFactory.getLogger(Clickevents.class);

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
        em.persist(s2);
        List<Userclicknodes> list = new ArrayList<>();
        for (Userclicknodes userclicknodes : sequenceList.getUserclicknodesSet()) {
            Userclicknodes u2 = em.find(Userclicknodes.class, userclicknodes.getId());
            u2.setSequenceList(s2);
            list.add(u2);
        }
        s2.setUserclicknodesSet(list);
        em.persist(s2);
        return s2;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) throws InterruptedException {
        logger.info(ConfigProvider.getConfig().getConfigSources().toString());
        Long value = em.createQuery("SELECT COUNT(s.id) FROM SequenceList s where s.deleted=0", Long.class).getSingleResult();
        if (value != null && value != 0) {
            Search.session(em).massIndexer(SequenceList.class).startAndWait();
        }
    }

    @GET
    @Path("sequence/search")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<SequenceList> search(@QueryParam("query") String query, @QueryParam("domain") String domain) {

//        final Function<SearchPredicateFactory, PredicateFinalStep> deletedFilter;
//        deletedFilter = f -> f.match().field("deleted").matching("0");
//        f -> f.bool().must(deletedFilter.apply(f)).must(domainFilter.apply(f)).must(f.matchAll());

        final Function<SearchPredicateFactory, PredicateFinalStep> domainFilter;
        final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
        if (domain != null && !domain.isEmpty()) {
            domainFilter = f -> f.match().field("domain").matching(domain);
        } else {
            domainFilter = null;
        }
        if (query == null || query.isEmpty()) {
            queryFunction = domainFilter == null ?
                    SearchPredicateFactory::matchAll :
                    f -> f.bool().must(domainFilter.apply(f)).must(f.matchAll());
        } else {
            queryFunction = domainFilter == null ?
                    f -> f.simpleQueryString().fields("name", "userclicknodesSet.clickednodename")
                            .matching(query) :
                    f -> f.bool().must(domainFilter.apply(f))
                            .must(f.simpleQueryString()
                                    .fields("name", "userclicknodesSet.clickednodename")
                            .matching(query));
        }

        return Search.session(em).search(SequenceList.class).predicate(queryFunction).fetchAll().getHits();
    }

    @POST
    @Path("sequence/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList deletesequence(SequenceList sequenceList){
        TypedQuery<SequenceList> query = em.createQuery("select sq from SequenceList sq where sq.id=:id", SequenceList.class);
        query.setParameter("id", sequenceList.getId());
        SequenceList dbresult = query.getSingleResult();
        if(dbresult == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        String dbuserid=dbresult.getUsersessionid();
        String sentuserid=sequenceList.getUsersessionid();
        if(dbuserid.equals(sentuserid)){
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
    public List<SequenceVotes> getsequencevotes(@PathParam("id") long id){
        /*if(id == null){
            return null;
        } else {*/
            System.out.println(id);
            TypedQuery<SequenceVotes> query = em.createQuery("select sqv from SequenceVotes sqv where sqv.sequenceid=:id", SequenceVotes.class);
            query.setParameter("id", id);
            return query.getResultList();
//        }
    }

    @POST
    @Path("sequence/addvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceVotes addsequencevote(SequenceVotes sequenceVotes){

        try {
            SequenceVotes dbsequenceVotes = sequenceVotesDAO.findbysequenceidusersessionid(sequenceVotes.getSequenceid(),sequenceVotes.getUsersessionid());
            if(dbsequenceVotes!=null){
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
    public void deletesequencevote(@PathParam("id") long id, @PathParam("usersessionid") String usersessionid){
        SequenceVotes dbsequenceVotes = sequenceVotesDAO.findbyusersessionid(usersessionid);
        if(dbsequenceVotes!=null){
            dbsequenceVotes.delete();
        }
    }
}