package com.nistapp.uda.index;

import com.nistapp.uda.index.models.DomainPatterns;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/domain")
public class Domain {
	@Inject
	EntityManager em;

	@GET
	@Path("/patterns")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DomainPatterns> listdatabyurl(@QueryParam("domain") @DefaultValue("NA") String domain) {
		TypedQuery<DomainPatterns> query = em.createQuery("select  e from DomainPatterns e where e.domain=:domain",DomainPatterns.class)
				.setParameter("domain",domain);
		return query.getResultList();
	}
}
