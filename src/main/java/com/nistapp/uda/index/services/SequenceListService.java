package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.repository.SequenceListDAO;
import com.nistapp.uda.index.utils.LoggerUtil;
import io.quarkus.security.Authenticated;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Path("/sequence")
@Authenticated
public class SequenceListService {

    private static final Logger logger = LoggerUtil.getLogger(SequenceList.class);

    @Inject
    EntityManager em;

    @Inject
    SequenceListDAO sequenceListDAO;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SequenceList updateSequenceData(SequenceList sequenceList) {
        try {
            logger.info("Updating sequence data for ID: " + sequenceList.getId().toString());

            // Fetch the existing entity from the database
            SequenceList existingSequence = sequenceListDAO.findById(sequenceList.getUsersessionid(), sequenceList.getId());

            if(existingSequence != null) {
                boolean hasChanges = false;

                // Check and update simple fields
                if (sequenceList.getName() != null && !Objects.equals(existingSequence.getName(), sequenceList.getName())) {
                    logger.debug("Field 'name' changed from '" + existingSequence.getName() + "' to '" + sequenceList.getName() + "'");
                    existingSequence.setName(sequenceList.getName());
                    hasChanges = true;
                }

                if (sequenceList.getDomain() != null && !Objects.equals(existingSequence.getDomain(), sequenceList.getDomain())) {
                    logger.debug("Field 'domain' changed from '" + existingSequence.getDomain() + "' to '" + sequenceList.getDomain() + "'");
                    existingSequence.setDomain(sequenceList.getDomain());
                    hasChanges = true;
                }

                if (sequenceList.getUserclicknodelist() != null && !Objects.equals(existingSequence.getUserclicknodelist(), sequenceList.getUserclicknodelist())) {
                    logger.debug("Field 'userclicknodelist' changed");
                    existingSequence.setUserclicknodelist(sequenceList.getUserclicknodelist());
                    hasChanges = true;
                }

                // Handle additionalParams separately as it's a complex object
                Map<String, Object> existingParams = existingSequence.getAdditionalParams();
                Map<String, Object> newParams = sequenceList.getAdditionalParams();

                if (newParams != null) {
                    Map<String, Object> mergedParams = new HashMap<>(existingParams != null ? existingParams : new HashMap<>());

                    for (Map.Entry<String, Object> entry : newParams.entrySet()) {
                        String key = entry.getKey();
                        Object newValue = entry.getValue();
                        Object existingValue = existingParams != null ? existingParams.get(key) : null;

                        // Check if the value has changed
                        if (!Objects.equals(newValue, existingValue)) {
                            logger.debug("AdditionalParam '" + key + "' changed from '" + existingValue + "' to '" + newValue + "'");
                            mergedParams.put(key, newValue);
                            hasChanges = true;
                        }
                    }

                    // Only update additionalParams if there are changes
                    if (hasChanges) {
                        existingSequence.setAdditionalParams(mergedParams);
                    }
                }

                // Only persist if there are actual changes
                if (hasChanges) {
                    em.persist(existingSequence);
                    logger.info("Updated sequence with ID: " + existingSequence.getId() + " - changes detected");
                } else {
                    logger.info("No changes detected for sequence with ID: " + existingSequence.getId());
                }

                return existingSequence;
            } else {
                logger.warn("Sequence with ID: " + sequenceList.getId() + " not found");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error updating sequence data", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }


    @POST
    @Path("/reindex/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response reindexSequence(@PathParam("id") Integer id) {
        try {
            logger.info("Reindexing sequence data for ID: " + id);

            // Fetch the existing entity from the database
            SequenceList sequenceToReindex = sequenceListDAO.findById(id);

            if (sequenceToReindex != null) {
                // Reindex the specific entity using Hibernate Search
                org.hibernate.search.mapper.orm.Search.session(em)
                        .indexingPlan()
                        .addOrUpdate(sequenceToReindex);

                logger.info("Successfully reindexed sequence with ID: " + id);

                return Response.ok(Map.of(
                        "success", true,
                        "message", "Sequence reindexed successfully"
                )).build();
            } else {
                logger.warn("Sequence with ID: " + id + " not found for reindexing");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of(
                                "success", false,
                                "message", "Sequence not found"
                        ))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error reindexing sequence data", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "success", false,
                            "message", "Error reindexing sequence: " + e.getMessage()
                    ))
                    .build();
        }
    }


}
