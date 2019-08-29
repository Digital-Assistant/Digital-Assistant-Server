package com.nistapp.voice.index;

import com.nistapp.voice.index.models.JavascriptEvents;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/clickevents")
public class Clickevents {

	@Inject
	EntityManager em;

	@GET
	@Path("/all")
	@Produces(MediaType.TEXT_PLAIN)
	public String listalldata() {
		return "hello";
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