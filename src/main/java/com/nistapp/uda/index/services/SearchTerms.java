package com.nistapp.uda.index.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nistapp.uda.index.models.ClickTrack;
import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceVotesDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Path("/searchTerms")
public class SearchTerms {

	private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;



	@Transactional
	void onStart(@Observes StartupEvent event) throws InterruptedException {
		logger.info(ConfigProvider.getConfig().getConfigSources().toString());
		Long value1 = em.createQuery("SELECT COUNT(ct.id) FROM ClickTrack ct where ct.clicktype='search'", Long.class).getSingleResult();
		if (value1 != null && value1 != 0) {
			Search.session(em).massIndexer(ClickTrack.class).startAndWait();
		}
	}

	@GET
	@Path("/all")
	@Transactional
	@Authenticated
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> search(@QueryParam("query") String query, @QueryParam("domain") String domain, @QueryParam("size") Optional<Integer> size, @QueryParam("page") Optional<Integer> page ) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
//		logger.info("--------------------------------------------------------------------------------------------------");
//		logger.info("Api invoked at:" + formatter.format(new Date()));

		Integer offset = 0;
		offset = page.orElse(0) * 10;

		final Function<SearchPredicateFactory, PredicateFinalStep> domainFilter;
		final Function<SearchPredicateFactory, PredicateFinalStep> eventTypeFilter;
		eventTypeFilter = f -> f.match().field("clicktype").matching("search");
		final Function<SearchPredicateFactory, PredicateFinalStep> queryFunction;
		if (domain != null && !domain.isEmpty()) {
			domainFilter = f -> f.match().field("domain").matching(domain);
		} else {
			domainFilter = null;
			throw new BadRequestException();
		}
		List<ClickTrack> searchresults;
		SearchQueryOptionsStep<?, ClickTrack, SearchLoadingOptionsStep, ?, ?> searchSession;
//		logger.info("elastic search results start time:" + formatter.format(new Date()));

		queryFunction = f -> {
			var search = f.bool();
			if(domainFilter != null) {
				search.must(domainFilter.apply(f)).must(eventTypeFilter.apply(f));
			}
			if(query == null || query.isEmpty()) {
				search.must(f.matchAll());
			} else {
				search.must(f.simpleQueryString().fields("clickedname").matching(query));
			}

			return search;
		};

		searchSession = Search.session(em).search(ClickTrack.class).where(queryFunction);
		if (query == null || query.isEmpty()){
			searchSession = searchSession.sort(f -> f.field("createdat_sort").desc());
		}

		logger.info(size.toString());
		searchresults = searchSession.fetchHits(offset, size.orElse(10));
		long totalHits = searchSession.fetchTotalHitCount();

		Map<String, Object> response = new HashMap<>();
		response.put("data", searchresults);
		response.put("totalHitCount", totalHits);

//		logger.info("elastic search results end time:" + formatter.format(new Date()));
//		logger.info("--------------------------------------------------------------------------------------------------");
		return response;
	}
}
