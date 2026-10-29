package com.nistapp.uda.index.users;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;

import jakarta.ws.rs.WebApplicationException;

/**
 * Decides, while a response is written, whether an owner value ({@code usersessionid}) may be shown to the caller.
 * Only the owner sees it: the Client compares it with its own id to show the owner's edit and delete controls.
 * Everyone else gets {@code null}, so search results no longer hand out other users' ids.
 */
public final class OwnerView {

	private OwnerView() {
	}

	/**
	 * @return {@code storedOwner} when the current request's caller owns it, otherwise {@code null} (also outside
	 * a request, or for an anonymous caller)
	 */
	public static String visibleTo(String storedOwner) {
		if (storedOwner == null) {
			return null;
		}
		ArcContainer container = Arc.container();
		if (container == null || !container.requestContext().isActive()) {
			return null;
		}
		// Not closed on purpose: closing a handle of a request-scoped bean would destroy the request's instance.
		InstanceHandle<CurrentUser> handle = container.instance(CurrentUser.class);
		try {
			CurrentUser currentUser = handle.get();
			if (currentUser == null || currentUser.isAnonymous()) {
				return null;
			}
			return currentUser.owns(storedOwner) ? storedOwner : null;
		} catch (WebApplicationException e) {
			// A token without a usable identity owns nothing.
			return null;
		}
	}
}
