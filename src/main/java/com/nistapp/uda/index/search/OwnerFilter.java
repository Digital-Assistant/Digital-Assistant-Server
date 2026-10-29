package com.nistapp.uda.index.search;

import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.List;
import java.util.function.Function;

/**
 * Search predicate for "owned by the caller": {@code usersessionid} equals any of the caller's owner values
 * ({@code CurrentUser.ownerKeys()}, taken from the token, never from a request parameter).
 */
public final class OwnerFilter {

	private OwnerFilter() {
	}

	public static Function<SearchPredicateFactory, PredicateFinalStep> ownedByAnyOf(List<String> ownerKeys) {
		return f -> {
			var owned = f.bool();
			for (String ownerKey : ownerKeys) {
				owned.should(f.match().field("usersessionid").matching(ownerKey));
			}
			return owned;
		};
	}
}
