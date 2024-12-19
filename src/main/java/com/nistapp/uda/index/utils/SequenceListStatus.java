package com.nistapp.uda.index.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SequenceListStatus {

    @Inject
    EntityManager em;

    public static final ConcurrentHashMap<String, Integer> statusCache = new ConcurrentHashMap<>();


    public Integer getPublishedStatusId() {
        return statusCache.computeIfAbsent("publishedStatus", key -> em.createQuery(
                        "SELECT s.id FROM Status s WHERE s.name = :name AND s.category = :category",
                        Integer.class)
                .setParameter("name", "published")
                .setParameter("category", "sequenceList")
                .getSingleResult());
    }

    public Integer getDraftStatusId() {
        return statusCache.computeIfAbsent("draftStatus", key -> em.createQuery(
                        "SELECT s.id FROM Status s WHERE s.name = :name AND s.category = :category",
                        Integer.class)
                .setParameter("name", "Draft")
                .setParameter("category", "sequenceList")
                .getSingleResult());
    }
}
