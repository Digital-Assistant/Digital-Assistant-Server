package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
import com.nistapp.uda.index.utils.SequenceListStatus;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@Path("/search")
public class SearchList {

	private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@Inject
	SequenceListStatus sequenceListStatus;



	@Transactional
	void onStart(@Observes StartupEvent event) throws InterruptedException {
		logger.info(ConfigProvider.getConfig().getConfigSources().toString());
		Long value = em.createQuery("SELECT COUNT(s.id) FROM SequenceList s where s.deleted=0 and s.isValid=1 and s.isIgnored=0", Long.class).getSingleResult();
		if (value != null && value != 0) {
			Search.session(em).massIndexer(SequenceList.class).startAndWait();
		}
	}

	@GET
	@Path("/all")
	@Transactional
	@Authenticated
	@Produces(MediaType.APPLICATION_JSON)
	public List<SequenceList> search(@QueryParam("query") String query, @QueryParam("domain") String domain, @QueryParam("size") Optional<Integer> size, @QueryParam("page") Optional<Integer> page, @QueryParam("userSessionId") String userSessionId) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
//		logger.info("--------------------------------------------------------------------------------------------------");
//		logger.info("Api invoked at:" + formatter.format(new Date()));

		Integer offset = 0;
		offset = page.orElse(0) * 10;

		Integer publishedStatusId = sequenceListStatus.getPublishedStatusId();

		final ArrayList<Function<SearchPredicateFactory, PredicateFinalStep>> additionalParamsFilter = new ArrayList<>();
		additionalParamsFilter.add(f -> f.bool()
				.should(f1 -> f1.match().field("additionalParams.status").matching(publishedStatusId.toString()))
				.should(f3 -> f3.bool().mustNot(m -> m.exists().field("additionalParams.status")))
		);
//		additionalParamsFilter.add(f -> f.bool().should(f1->f1.match().field("additionalParams.published").matching("true")).should(f3 -> f3.bool().mustNot(m -> m.exists().field("additionalParams.published"))));

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
//		logger.info("elastic search results start time:" + formatter.format(new Date()));

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

		queryFunction = f -> {
			var search = f.bool().must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f));
			if(domainFilter != null) {
				search.must(domainFilter.apply(f));
			}
			search.must(mustShouldFunction.apply(f));
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

//		logger.info("elastic search results end time:" + formatter.format(new Date()));
//		logger.info("--------------------------------------------------------------------------------------------------");
		return searchresults;
	}
}
