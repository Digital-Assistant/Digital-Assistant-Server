package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.Status;
import io.quarkus.security.Authenticated;
import org.hibernate.search.mapper.orm.Search;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST service for managing Status entities.
 * This service provides CRUD operations and specialized queries for Status management.
 * All endpoints require authentication.
 *
 * @author Your Organization
 * @version 1.0
 * @since 1.0
 *
 * @see Status
 * @see EntityManager
 */
@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class StatusService {

    /**
     * Injected EntityManager for database operations
     */
    @Inject
    EntityManager em;


    private static final ConcurrentHashMap<String, List<Status>> categoryStatusCache = new ConcurrentHashMap<>();

    private List<Status> getActiveSequenceListStatuses() {
        return categoryStatusCache.computeIfAbsent("sequenceListStatuses", key -> em.createQuery(
                        "SELECT s FROM Status s WHERE s.category = :category AND s.isActive = :isActive",
                        Status.class)
                .setParameter("category", "sequenceList")
                .setParameter("isActive", true)
                .getResultList());
    }

    /**
     * Retrieves all statuses with optional active status filtering.
     *
     * @param isActive Optional parameter to filter active/inactive statuses. Defaults to true if not provided.
     * @return List<Status> List of statuses matching the filter criteria
     * @throws WebApplicationException if database access fails
     */
    @GET
    @Path("/all")
    @Transactional
    public List<Status> getAllStatuses(@QueryParam("isActive") Optional<Boolean> isActive) {
        Boolean activeFilter = isActive.orElse(true);
        return em.createQuery("SELECT s FROM Status s WHERE s.isActive = :isActive", Status.class)
                .setParameter("isActive", activeFilter)
                .getResultList();
    }

    /**
     * Retrieves statuses filtered by category with optional active status filtering.
     *
     * @param category The category to filter by
     * @param isActive Optional parameter to filter active/inactive statuses. Defaults to true if not provided.
     * @return List<Status> List of statuses matching the category and filter criteria
     * @throws WebApplicationException if database access fails
     */
    @GET
    @Path("/category/{category}")
    @Transactional
    public List<Status> getStatusesByCategory(
            @PathParam("category") String category,
            @QueryParam("isActive") Optional<Boolean> isActive) {
        Boolean activeFilter = isActive.orElse(true);
        String cacheKey = category + "-" + activeFilter;

        return categoryStatusCache.computeIfAbsent(cacheKey, key ->
                em.createQuery(
                                "SELECT s FROM Status s WHERE s.category = :category AND s.isActive = :isActive ORDER BY s.order",
                                Status.class)
                        .setParameter("category", category)
                        .setParameter("isActive", activeFilter)
                        .getResultList()
        );
    }

    /**
     * Retrieves a single status by its ID.
     *
     * @param id The unique identifier of the status
     * @return Response containing the Status if found
     * @throws WebApplicationException with 404 status if status not found
     */
    @GET
    @Path("/{id}")
    @Transactional
    public Response getStatus(@PathParam("id") Integer id) {
        Status status = em.find(Status.class, id);
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(status).build();
    }

    /**
     * Creates a new status entry.
     *
     * @param status The Status object to be created
     * @return Response with status 201 and created Status entity
     * @throws WebApplicationException with 400 status if status has an ID
     */
    @POST
    @Transactional
    public Response createStatus(Status status) {
        if (status.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("New status should not have an ID").build();
        }
        em.persist(status);
        return Response.status(Response.Status.CREATED).entity(status).build();
    }

    /**
     * Updates an existing status.
     *
     * @param id The unique identifier of the status to update
     * @param status The updated Status data
     * @return Response with updated Status entity
     * @throws WebApplicationException with 404 status if status not found
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateStatus(@PathParam("id") Integer id, Status status) {
        Status existingStatus = em.find(Status.class, id);
        if (existingStatus == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        existingStatus.setName(status.getName());
        existingStatus.setDescription(status.getDescription());
        existingStatus.setCategory(status.getCategory());
        existingStatus.setOrder(status.getOrder());
        existingStatus.setIsActive(status.getIsActive());

        em.merge(existingStatus);
        return Response.ok(existingStatus).build();
    }

    /**
     * Deletes a status by its ID.
     *
     * @param id The unique identifier of the status to delete
     * @return Response with 204 status if successful
     * @throws WebApplicationException with 404 status if status not found
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteStatus(@PathParam("id") Integer id) {
        Status status = em.find(Status.class, id);
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.remove(status);
        return Response.noContent().build();
    }

    /**
     * Toggles the active state of a status.
     *
     * @param id The unique identifier of the status to toggle
     * @return Response with updated Status entity
     * @throws WebApplicationException with 404 status if status not found
     */
    @PATCH
    @Path("/{id}/toggle-active")
    @Transactional
    public Response toggleStatusActive(@PathParam("id") Integer id) {
        Status status = em.find(Status.class, id);
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        status.setIsActive(!status.getIsActive());
        em.merge(status);
        return Response.ok(status).build();
    }
}
