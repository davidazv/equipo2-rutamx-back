package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.models.BusModel;
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

    @POST
    @Transactional
    public Response createBusModel(BusModel model) {
        try {
            BusModel created = busModelRepository.create(model);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("MODEL_ALREADY_EXISTS").build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateBusModel(@PathParam("id") Long id, BusModel model) {
        try {
            BusModel updated = busModelRepository.update(id, model);
            return Response.ok(updated).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("MODEL_NOT_FOUND").build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("MODEL_ALREADY_EXISTS").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteBusModel(@PathParam("id") Long id) {
        try {
            busModelRepository.delete(id);
            return Response.noContent().build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("MODEL_NOT_FOUND").build();
        }
    }
}
