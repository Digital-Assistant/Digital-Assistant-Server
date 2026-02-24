package com.nistapp.uda.index.users;

import io.quarkus.security.Authenticated;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin")
@Authenticated
public class Admin {

	/**
	 * A description of the entire Java function.
	 *
	 * @return allowed or not description of return value
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String admin() {
		return "granted";
	}

}
