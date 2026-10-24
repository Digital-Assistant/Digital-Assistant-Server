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
			searchMapping.scope(entity).massIndexer().startAndWait();
			long indexed = Search.session(em).search(entity).where(f -> f.matchAll()).fetchTotalHitCount();
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
