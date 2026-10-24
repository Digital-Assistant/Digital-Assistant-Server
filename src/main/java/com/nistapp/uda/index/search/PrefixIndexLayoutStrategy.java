package com.nistapp.uda.index.search;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.search.backend.elasticsearch.index.layout.IndexLayoutStrategy;

/**
 * Same layout as Hibernate Search's "simple" strategy ({@code <name>-000001} plus {@code -read} and
 * {@code -write} aliases), with {@code udan.search.index-prefix} put in front of every name. Lets TEST and PROD
 * share one Elasticsearch without sharing indexes. The default (empty) prefix gives the names prod already uses.
 */
@ApplicationScoped
public class PrefixIndexLayoutStrategy implements IndexLayoutStrategy {

	private static final Pattern PREFIX_FORMAT = Pattern.compile("[a-z0-9_.-]*");
	private static final Pattern ES_INDEX_NAME = Pattern.compile("(.*)-\\d{6}");

	@ConfigProperty(name = "udan.search.index-prefix")
	Optional<String> configuredPrefix;

	private String prefix;

	public PrefixIndexLayoutStrategy() {
	}

	PrefixIndexLayoutStrategy(String prefix) {
		this.prefix = validate(prefix);
	}

	private String prefix() {
		if (prefix == null) {
			prefix = validate(configuredPrefix.orElse(""));
		}
		return prefix;
	}

	private static String validate(String value) {
		// Elasticsearch index names are lowercase; reject a bad prefix at startup, not on the first request.
		if (!PREFIX_FORMAT.matcher(value).matches()) {
			throw new IllegalArgumentException(
					"udan.search.index-prefix may only contain a-z, 0-9, '_', '.' and '-': '" + value + "'");
		}
		return value;
	}

	@Override
	public String createInitialElasticsearchIndexName(String hibernateSearchIndexName) {
		return prefix() + hibernateSearchIndexName + "-000001";
	}

	@Override
	public String createWriteAlias(String hibernateSearchIndexName) {
		return prefix() + hibernateSearchIndexName + "-write";
	}

	@Override
	public String createReadAlias(String hibernateSearchIndexName) {
		return prefix() + hibernateSearchIndexName + "-read";
	}

	@Override
	public String extractUniqueKeyFromHibernateSearchIndexName(String hibernateSearchIndexName) {
		return prefix() + hibernateSearchIndexName;
	}

	@Override
	public String extractUniqueKeyFromElasticsearchIndexName(String elasticsearchIndexName) {
		Matcher matcher = ES_INDEX_NAME.matcher(elasticsearchIndexName);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Unexpected Elasticsearch index name '" + elasticsearchIndexName
					+ "': expected the form <name>-NNNNNN");
		}
		return matcher.group(1);
	}
}
