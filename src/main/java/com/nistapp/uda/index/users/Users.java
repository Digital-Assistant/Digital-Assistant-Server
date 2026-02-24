package com.nistapp.uda.index.users;

import io.quarkus.security.identity.SecurityIdentity;
import org.jboss.resteasy.annotations.cache.NoCache;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

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
