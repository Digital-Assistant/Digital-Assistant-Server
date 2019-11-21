package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;
import com.nistapp.voice.index.models.SequenceList;
import com.nistapp.voice.index.models.Userclicknodes;
import io.quarkus.runtime.StartupEvent;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Path("/clickevents")
public class Clickevents {

    @Inject
    EntityManager em;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
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
    public List<JavascriptEvents> listdatabyurl(@QueryParam("url") @DefaultValue("NA") String url) {
        TypedQuery<JavascriptEvents> query = em.createQuery("select  e from JavascriptEvents e where e.urlpath=:url", JavascriptEvents.class).setParameter("url", url);
        return query.getResultList();
    }

    @GET
    @Path("/fetchrecorddata")
    @Produces(MediaType.APPLICATION_JSON)
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
        em.persist(sequenceList);
        return sequenceList;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) throws InterruptedException {
        Long value = em.createQuery("SELECT COUNT(s.id) FROM SequenceList s", Long.class).getSingleResult();
        if (value != null && value != 0) {
            Search.session(em).massIndexer(SequenceList.class).startAndWait();
        }
    }

    @GET
    @Path("sequnce/search")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<SequenceList> search(@QueryParam("query") String query) {

        Function<SearchPredicateFactory, PredicateFinalStep> function = f -> query == null || query.trim().isEmpty() ?
                f.matchAll() :
                f.simpleQueryString()
                        .fields("name").matching(query);

        return Search.session(em).search(SequenceList.class).predicate(function).fetchAll().getHits();
    }
}