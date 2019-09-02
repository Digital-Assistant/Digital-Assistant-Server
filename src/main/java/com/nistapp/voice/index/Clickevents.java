package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/clickevents")
public class Clickevents {

	@Inject
	EntityManager em;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public (Collection<JavascriptEvents>) listalldata() {
		Query query =  em.createQuery(JavascriptEvents.class);
		return (Collection<JavascriptEvents>) query.getResultList();
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

}