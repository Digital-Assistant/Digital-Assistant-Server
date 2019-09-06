package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;
import org.hibernate.Criteria;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
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
	//@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JavascriptEvents> listdatabyurl(@QueryParam("url") @DefaultValue("NA") String url) {
		Query query = em.createQuery("select  e from JavascriptEvents e where e.url=:url",JavascriptEvents.class).setParameter("url",url);
		return query.getResultList();
	}

}