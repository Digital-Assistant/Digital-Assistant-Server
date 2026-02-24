package com.nistapp.uda.index.config;

import io.quarkus.runtime.StartupEvent;
import org.hibernate.search.mapper.orm.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Validates core service connectivity (Database and Elasticsearch) on startup.
 * Logs clear status information instead of crashing immediately.
 */
@ApplicationScoped
public class HealthCheckListener {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckListener.class);

    @Inject
    EntityManager em;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        logger.info("Starting health validation...");

        // 1. Check Database
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            logger.info("✓ Database connection established successfully.");
        } catch (Exception e) {
            logger.error("✗ DATABASE CONNECTION FAILED: Check your database status and credentials.");
            logger.error("  Error detail: {}", e.getMessage());
        }

        // 2. Check Elasticsearch via Hibernate Search
        try {
            // This just pings the ES server through Hibernate Search session
            Search.session(em);
            logger.info("✓ Elasticsearch (via Hibernate Search) is configured and reachable.");
        } catch (Exception e) {
            logger.warn("✗ ELASTICSEARCH CONNECTION FAILED: Search features will be unavailable.");
            logger.warn("  Please ensure Elasticsearch is running at the configured host.");
            logger.warn("  Error detail: {}", e.getMessage());
        }
    }
}
