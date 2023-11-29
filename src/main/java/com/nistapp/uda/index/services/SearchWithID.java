package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
import io.quarkus.security.Authenticated;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.function.Function;

@Path("/search")
@Authenticated
public class SearchWithID {

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@GET
	@Path("{id}")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<SequenceList> SearchWithID(@PathParam("id") int id, @QueryParam("domain") String domain) {

		final Function<SearchPredicateFactory, PredicateFinalStep> deletedFilter;
		deletedFilter = f -> f.match().field("deleted").matching(0);

		final Function<SearchPredicateFactory, PredicateFinalStep> validFilter;
		validFilter = f -> f.match().field("isValid").matching(1);

		final Function<SearchPredicateFactory, PredicateFinalStep> ignoreFilter;
		ignoreFilter = f -> f.match().field("isIgnored").matching(0);

		final Function<SearchPredicateFactory, PredicateFinalStep> domainFilter;
		final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
		if (domain != null && !domain.isEmpty()) {
			domainFilter = f -> f.match().field("domain").matching(domain);
		} else {
			domainFilter = null;
			throw new BadRequestException();
		}

		final Function<SearchPredicateFactory, PredicateFinalStep> idFilter;
		idFilter = f -> f.match().field("id").matching(id);

		Optional<SequenceList> searchresults;
		SearchQueryOptionsStep<?, SequenceList, SearchLoadingOptionsStep, ?, ?> searchSession;

		queryFunction = f -> {
			var search = f.bool().must(idFilter.apply(f)).must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f));
			if(domainFilter != null) {
				search.must(domainFilter.apply(f));
			}

			search.must(f.matchAll());

			return search;
		};

		searchSession = Search.session(em).search(SequenceList.class).where(queryFunction);
		searchSession = searchSession.sort(f -> f.field("createdat_sort").desc());
		searchresults = searchSession.fetchSingleHit();

		return searchresults;
	}
}
