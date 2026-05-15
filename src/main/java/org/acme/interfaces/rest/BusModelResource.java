package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.repository.BusModelRepository;

import java.util.logging.Logger;

@Path("/api/bus-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BusModelResource {

    private static final Logger log = Logger.getLogger(BusModelResource.class.getName());

    private final BusModelRepository busModelRepository;

    @Inject
    public BusModelResource(BusModelRepository busModelRepository) {
        this.busModelRepository = busModelRepository;
    }

    @GET
    public Response listBusModels() {
        return Response.ok(busModelRepository.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response getBusModel(@PathParam("id") Long id) {
        return busModelRepository.findById(id)
                .map(m -> Response.ok(m).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Modelo de bus no encontrado").build());
    }
}
