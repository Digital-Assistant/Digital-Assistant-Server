package com.nistapp.voice.index.services;

import com.nistapp.voice.index.Clickevents;
import com.nistapp.voice.index.models.SequenceList;
import com.nistapp.voice.index.repository.SequenceVotesDAO;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Path("/search")
public class SearchWithID {
	private static final Logger logger = LoggerFactory.getLogger(Clickevents.class);

	@Inject
	EntityManager em;

	@Inject
	SequenceVotesDAO sequenceVotesDAO;

	@GET
	@Path("{id}")
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<SequenceList> SearchWithID(@PathParam("id") int id, @QueryParam("domain") String domain, @QueryParam("additionalParams") Optional<String> additionalParams) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
		logger.info("--------------------------------------------------------------------------------------------------");
		logger.info("Api invoked at:" + formatter.format(new Date()));

		String jsonString = additionalParams.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("Optional","").replaceAll("\\{","").replaceAll("\\}","").replaceAll(".empty","").toString();

		final ArrayList<Function<SearchPredicateFactory, PredicateFinalStep>> additionalParamsFilter = new ArrayList<>();

		String[] params = jsonString.split(",");

		if(params.length>0) {
			for (int i = 0; i < params.length; i++) {
				if(!params[i].isEmpty() && params[i].toString() != ".empty") {
					String[] param = params[i].toString().replaceAll("\"", "").split(":");
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

		final Function<SearchPredicateFactory, PredicateFinalStep> idFilter;
		idFilter = f -> f.match().field("id").matching(id);

		Optional<SequenceList> searchresults;
		SearchQueryOptionsStep<?, SequenceList, SearchLoadingOptionsStep, ?, ?> searchSession;
		logger.info("elastic search results start time:" + formatter.format(new Date()));

		queryFunction = f -> {
			var search = f.bool().must(idFilter.apply(f)).must(deletedFilter.apply(f)).must(validFilter.apply(f)).must(ignoreFilter.apply(f));
			if(domainFilter != null) {
				search.must(domainFilter.apply(f));
			}
			if(additionalParamsFilter.size() > 0) {
				for (Function<SearchPredicateFactory, PredicateFinalStep> additionalParam : additionalParamsFilter) {
					search.must(additionalParam.apply(f));
				}
			}

			search.must(f.matchAll());

			return search;
		};

		searchSession = Search.session(em).search(SequenceList.class).where(queryFunction);
		searchSession = searchSession.sort(f -> f.field("createdat_sort").desc());
		searchresults = searchSession.fetchSingleHit();

		logger.info("elastic search results end time:" + formatter.format(new Date()));
		logger.info("--------------------------------------------------------------------------------------------------");
		return searchresults;
	}
}
