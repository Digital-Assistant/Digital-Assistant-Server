package com.nistapp.uda.index;

import com.nistapp.uda.index.models.DomainPatterns;
import io.quarkus.security.Authenticated;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/domain")
public class Domain {
	@Inject
	EntityManager em;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String defaultResponse(){
		return "Site is up";
	}

	@GET
	@Path("/patterns")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DomainPatterns> listdatabyurl(@QueryParam("domain") @DefaultValue("NA") String domain) {
		TypedQuery<DomainPatterns> query = em.createQuery("select  e from DomainPatterns e where e.domain=:domain",DomainPatterns.class)
				.setParameter("domain",domain);
		return query.getResultList();
	}
}
