package com.nistapp.uda.index.search;

import com.nistapp.uda.index.models.ClickTrack;
import com.nistapp.uda.index.models.SequenceList;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fills the Elasticsearch indexes from the database at startup.
 * <p>
 * With {@code udan.search.reindex-on-startup=true} (default) it re-indexes every time, as before. With {@code false}
 * it only fills an index that is empty while the table has rows (first start with a new index prefix).
 * Failures are logged as ERROR, and the counts are logged so deploy checks can read them.
 * <p>
 * The mass indexer is throttled (one loading thread, small batches, one type at a time) because some documents are
 * large and a small shared Elasticsearch trips its circuit breaker under the default parallel load. A run that fails
 * or ends with fewer documents than rows is repeated once.
 */
@ApplicationScoped
public class StartupReindexer {

	private static final Logger logger = LoggerFactory.getLogger(StartupReindexer.class);

	@Inject
	EntityManager em;

	@Inject
	SearchMapping searchMapping;

	@ConfigProperty(name = "udan.search.reindex-on-startup", defaultValue = "true")
	boolean reindexOnStartup;

	@ConfigProperty(name = "udan.search.reindex.threads", defaultValue = "1")
	int threads;

	@ConfigProperty(name = "udan.search.reindex.batch-size", defaultValue = "5")
	int batchSize;

	@ConfigProperty(name = "udan.search.reindex.attempts", defaultValue = "2")
	int attempts;

	void onStart(@Observes StartupEvent event) {
		reindexIfNeeded(SequenceList.class);
		reindexIfNeeded(ClickTrack.class);
	}

	@ActivateRequestContext
	void reindexIfNeeded(Class<?> entity) {
		String name = entity.getSimpleName();
		try {
			long rows = em.createQuery("SELECT COUNT(e) FROM " + name + " e", Long.class).getSingleResult();
			long before = Search.session(em).search(entity).where(f -> f.matchAll()).fetchTotalHitCount();
			if (rows == 0) {
				logger.info("Search index {}: table is empty, nothing to index.", name);
				return;
			}
			if (!reindexOnStartup && before > 0) {
				logger.info("Search index {}: startup re-index is off and the index has {} documents ({} rows), skipped.",
						name, before, rows);
				return;
			}
			logger.info("Search index {}: re-indexing {} rows (index had {} documents, reindex-on-startup={}).",
					name, rows, before, reindexOnStartup);
			long start = System.currentTimeMillis();
			long indexed = -1;
			for (int attempt = 1; attempt <= Math.max(1, attempts); attempt++) {
				try {
					searchMapping.scope(entity).massIndexer()
							.threadsToLoadObjects(threads)
							.batchSizeToLoadObjects(batchSize)
							.typesToIndexInParallel(1)
							.startAndWait();
				} catch (RuntimeException e) {
					logger.error("Search index {}: re-index attempt {} failed (database or Elasticsearch).", name, attempt, e);
				}
				indexed = Search.session(em).search(entity).where(f -> f.matchAll()).fetchTotalHitCount();
				if (indexed == rows) {
					break;
				}
				if (attempt < attempts) {
					logger.warn("Search index {}: attempt {} left {} documents for {} rows, trying again.",
							name, attempt, indexed, rows);
					Thread.sleep(5000);
				}
			}
			long seconds = (System.currentTimeMillis() - start) / 1000;
			if (indexed != rows) {
				logger.error("Search index {}: re-index finished in {}s but the index has {} documents for {} rows.",
						name, seconds, indexed, rows);
			} else {
				logger.info("Search index {}: re-indexed {} documents in {}s.", name, indexed, seconds);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Search index {}: re-index interrupted.", name, e);
		} catch (Exception e) {
			logger.error("Search index {}: re-index at startup failed (database or Elasticsearch).", name, e);
		}
	}
}
