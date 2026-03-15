package com.nistapp.uda.index.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SequenceListStatus {

    @Inject
    EntityManager em;

    public static final ConcurrentHashMap<String, Integer> statusCache = new ConcurrentHashMap<>();

    @Transactional
    public Integer getPublishedStatusId() {
        return statusCache.computeIfAbsent("publishedStatus", key -> em.createQuery(
                        "SELECT s.id FROM Status s WHERE s.name = :name AND s.category = :category",
                        Integer.class)
                .setParameter("name", "published")
                .setParameter("category", "sequenceList")
                .getSingleResult());
    }

    @Transactional
    public Integer getDraftStatusId() {
        return statusCache.computeIfAbsent("draftStatus", key -> em.createQuery(
                        "SELECT s.id FROM Status s WHERE s.name = :name AND s.category = :category",
                        Integer.class)
                .setParameter("name", "Draft")
                .setParameter("category", "sequenceList")
                .getSingleResult());
    }
}
