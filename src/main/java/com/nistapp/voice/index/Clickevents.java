package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;
import com.nistapp.voice.index.models.SequenceList;
import com.nistapp.voice.index.models.Userclicknodes;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Path("/clickevents")
public class Clickevents {

	@Inject
	EntityManager em;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<JavascriptEvents> listalldata() {
		Query query = em.createQuery("from JavascriptEvents e",JavascriptEvents.class);
		return query.getResultList();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public JavascriptEvents indexeddata(JavascriptEvents javascriptevents) {
		em.persist(javascriptevents);
		em.flush();
		return javascriptevents;
	}

	@GET
	@Path("/fetchbyurl")
//	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JavascriptEvents> listdatabyurl(@QueryParam("url") @DefaultValue("NA") String url) {
		Query query = em.createQuery("select  e from JavascriptEvents e where e.url=:url",JavascriptEvents.class).setParameter("url",url);
		return query.getResultList();
	}

	@GET
	@Path("/fetchrecorddata")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Userclicknodes> getuserrecordeddata(@QueryParam("start") long starttimestamp, @QueryParam("end") long endtimestamp, @QueryParam("sessionid") String sessionid, @QueryParam("domain") String domain) {
		Date startdate = new Date(starttimestamp);
		Date enddate = new Date(endtimestamp);
		Timestamp startdatetime = new Timestamp(startdate.getTime());
		Timestamp enddatetime = new Timestamp(enddate.getTime());
		Query query = em.createQuery("select u from Userclicknodes u where u.sessionid=:sessionid and u.domain=:domain and u.createdat between :startdate and :enddate",Userclicknodes.class);
		query.setParameter("sessionid",sessionid).setParameter("domain",domain).setParameter("startdate",startdatetime).setParameter("enddate",enddatetime);
//		System.out.println(startdatetime.toString());
//		System.out.println("starttimestamp:"+startdatetime);
//		System.out.println("endtimestamp:"+enddatetime);
		return query.getResultList();
	}

	@GET
	@Path("/fetchtimestamp")
	@Produces(MediaType.TEXT_PLAIN)
	public long gettimestamp(){
		Date date= new Date();
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
		em.flush();
//		sequenceList.clicknodesdata=sequenceList.getClicknodesdata();
		return sequenceList;
	}
}