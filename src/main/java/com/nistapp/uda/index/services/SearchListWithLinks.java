package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
import com.nistapp.uda.index.utils.SequenceListStatus;
import com.nistapp.uda.index.views.SearchListView;
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
public class SearchListWithLinks {

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@Inject
	SequenceListStatus sequenceListStatus;

	@GET
	@Path("links")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public List<SequenceList> SearchWithPermissions(@QueryParam("query") String query, @QueryParam("domain") String domain, @QueryParam("size") Optional<Integer> size, @QueryParam("additionalParams") Optional<String> additionalParams, @QueryParam("page") Optional<Integer> page) {

		Integer offset = 0;
		offset = page.orElse(0) * 10;

		String jsonString = additionalParams.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("Optional","").replaceAll("\\{","").replaceAll("\\}","").replaceAll(".empty","");

		Integer publishedStatusId = sequenceListStatus.getPublishedStatusId();

		final ArrayList<Function<SearchPredicateFactory, PredicateFinalStep>> additionalParamsFilter = new ArrayList<>();
		additionalParamsFilter.add(f -> f.bool()
				.should(f1 -> f1.match().field("additionalParams.status").matching(publishedStatusId.toString()))
				.should(f3 -> f3.bool().mustNot(m -> m.exists().field("additionalParams.status")))
		);

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
		final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
		if (domain != null && !domain.isEmpty()) {
			domainFilter = f -> f.match().field("domain").matching(domain);
		} else {
			domainFilter = null;
			throw new BadRequestException();
		}
		List<SequenceList> searchresults;
		SearchQueryOptionsStep<?, SequenceList, SearchLoadingOptionsStep, ?, ?> searchSession;

		queryFunction = f -> {
			var search = f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f));
			if(domainFilter != null) {
				search.must(domainFilter.apply(f));
			}
			if(additionalParamsFilter.size() > 0) {
				for (Function<SearchPredicateFactory, PredicateFinalStep> additionalParam : additionalParamsFilter) {
					search.must(additionalParam.apply(f));
				}
			}
			if(query == null || query.isEmpty()) {
				search.must(f.matchAll());
			} else {
				search.must(f.simpleQueryString().fields("name").matching(query));
			}
			return search;
		};

		searchSession = Search.session(em).search(SequenceList.class).where(queryFunction);

		if (query == null || query.isEmpty()){
			searchSession = searchSession.sort(f -> f.field("createdat_sort").desc());
		}

		searchresults = searchSession.fetchHits(offset, size.orElse(10));


		/*SimpleBeanPropertyFilter simpleBeanPropertyFilter =
				SimpleBeanPropertyFilter.serializeAllExcept("userclicknodesSet", "additionalParams");

		FilterProvider filterProvider = new SimpleFilterProvider()
				.addFilter("customFilter", simpleBeanPropertyFilter);

		MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userList);
		mappingJacksonValue.setFilters(filterProvider);*/


		return searchresults;
	}
}
