package voice.backend.indexing;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/clickevents")
public class Clickevents {

    @GET
    @Path("/all")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @PUT
    @Path("/push")
    @Produces(MediaType.TEXT_PLAIN)
    public
}