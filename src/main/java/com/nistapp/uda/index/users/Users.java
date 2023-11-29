package com.nistapp.uda.index.users;

import io.quarkus.security.identity.SecurityIdentity;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/users")
public class Users {

	@Inject
	SecurityIdentity identity;

	@GET
	@Path("/me")
	@RolesAllowed("user")
	@NoCache
	public User me() {
		return new User(identity);
	}

	public static class User {

		private final String userName;

		User(SecurityIdentity identity) {
			this.userName = identity.getPrincipal().getName();
		}

		public String getUserName() {
			return userName;
		}
	}
}
