package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Path("/search")
public class SearchWithPermissions {

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@GET
	@Path("withPermissions")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public List<SequenceList> SearchWithPermissions(@QueryParam("query") String query, @QueryParam("domain") String domain, @QueryParam("size") Optional<Integer> size, @QueryParam("additionalParams") Optional<String> additionalParams, @QueryParam("userSessionId") String userSessionId) {

		String jsonString = additionalParams.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("Optional","").replaceAll("\\{","").replaceAll("\\}","").replaceAll(".empty","");

		final ArrayList<Function<SearchPredicateFactory, PredicateFinalStep>> additionalParamsFilter = new ArrayList<>();

		String[] params = jsonString.split(",");

		if(params.length>0) {
			for (int i = 0; i < params.length; i++) {
				if(!params[i].isEmpty() && params[i] != ".empty") {
					String[] param = params[i].replaceAll("\"", "").split(":");
					additionalParamsFilter.add(f -> f.bool().should(f1->f1.match().field("additionalParams."+param[0]).matching(param[1])).should(f2 -> f2.match().field("additionalParams."+param[0]).matching("0")));
				}
			}
		}

		final Function<SearchPredicateFactory, PredicateFinalStep> deletedFilter;
		deletedFilter = f -> f.match().field("deleted").matching(0);

		final Function<SearchPredicateFactory, PredicateFinalStep> validFilter;
		validFilter = f -> f.match().field("isValid").matching(1);

		final Function<SearchPredicateFactory, PredicateFinalStep> ignoreFilter;
		ignoreFilter = f -> f.match().field("isIgnored").matching(0);

		final Function<SearchPredicateFactory, PredicateFinalStep> domainFilter;
		if (domain != null && !domain.isEmpty()) {
			domainFilter = f -> f.match().field("domain").matching(domain);
		} else {
			domainFilter = null;
			throw new BadRequestException();
		}

		final Function<SearchPredicateFactory, PredicateFinalStep> userFilter;
		userFilter = f -> f.match().field("usersessionid").matching(userSessionId);

		final Function<SearchPredicateFactory, PredicateFinalStep> mustFunction;
		mustFunction = f -> {
			var searchMust = f.bool();
			if(additionalParamsFilter.size() > 0) {
				for (Function<SearchPredicateFactory, PredicateFinalStep> additionalParam : additionalParamsFilter) {
					searchMust.must(additionalParam.apply(f));
				}
			}
			if(query == null || query.isEmpty()) {
				searchMust.must(f.matchAll());
			} else {
				searchMust.must(f.simpleQueryString().fields("name").matching(query));
			}

			return searchMust;
		};

		final Function<SearchPredicateFactory, PredicateFinalStep> shouldFunction;
		shouldFunction = f -> {
			var searchShould = f.bool();
			searchShould.should(userFilter.apply(f));
			return searchShould;
		};

		final Function<SearchPredicateFactory, PredicateFinalStep> mustShouldFunction;
		mustShouldFunction = f -> {
			var searchShouldMust = f.bool().should(mustFunction.apply(f)).should(shouldFunction.apply(f));
			return searchShouldMust;
		};

		final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
		queryFunction = f -> {
			var search = f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f));
			if(domainFilter != null) {
				search.must(domainFilter.apply(f));
			}
			search.must(mustShouldFunction.apply(f));
			return search;
		};

		SearchQueryOptionsStep<?, SequenceList, SearchLoadingOptionsStep, ?, ?> searchSession;
		searchSession = Search.session(em).search(SequenceList.class).where(queryFunction);

		if (query == null || query.isEmpty()){
			searchSession = searchSession.sort(f -> f.field("createdat_sort").desc());
		}

		List<SequenceList> searchResults;
		searchResults = searchSession.fetchHits(size.orElse(10));

		return searchResults;
	}
}
