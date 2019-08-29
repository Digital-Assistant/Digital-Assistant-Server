package voice.backend.indexing;

import voice.backend.indexing.models.JavascriptEvents;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/clickevents")
public class Clickevents {

    @Inject
    EntityManager em;

    @GET
    @Path("/all")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @POST
    @Path("/push")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public JavascriptEvents indexeddata(JavascriptEvents javascriptevents){
        em.persist(javascriptevents);
        return javascriptevents;
    }

}