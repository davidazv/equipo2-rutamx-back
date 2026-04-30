package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.dto.CreateBusModelDto;
import org.acme.application.dto.UpdateBusModelDto;
import org.acme.application.exception.BusModelAlreadyExistsException;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.usecase.CreateBusModelUseCase;
import org.acme.application.usecase.DeleteBusModelUseCase;
import org.acme.application.usecase.UpdateBusModelUseCase;
import org.acme.domain.repository.BusModelRepository;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

@Path("/api/bus-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BusModelResource {

    private static final Logger log = Logger.getLogger(BusModelResource.class.getName());

    private final BusModelRepository busModelRepository;
    private final CreateBusModelUseCase createBusModelUseCase;
    private final UpdateBusModelUseCase updateBusModelUseCase;
    private final DeleteBusModelUseCase deleteBusModelUseCase;

    @Inject
    public BusModelResource(BusModelRepository busModelRepository,
                            CreateBusModelUseCase createBusModelUseCase,
                            UpdateBusModelUseCase updateBusModelUseCase,
                            DeleteBusModelUseCase deleteBusModelUseCase) {
        this.busModelRepository = busModelRepository;
        this.createBusModelUseCase = createBusModelUseCase;
        this.updateBusModelUseCase = updateBusModelUseCase;
        this.deleteBusModelUseCase = deleteBusModelUseCase;
    }

    @GET
    public Response listBusModels() {
        return Response.ok(busModelRepository.findAll()).build();
    }

    @POST
    public Response createBusModel(CreateBusModelDto dto) {
        try {
            var created = createBusModelUseCase.execute(dto);
            return Response.created(URI.create("/api/bus-models/" + created.getId()))
                    .entity(Map.of(
                            "message", "Modelo agregado correctamente",
                            "model", created
                    )).build();
        } catch (BusModelAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Ya existe un modelo con ese nombre").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Tipo de combustible inválido").build();
        } catch (Exception e) {
            log.severe("Error creando modelo: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getBusModel(@PathParam("id") Long id) {
        return busModelRepository.findById(id)
                .map(m -> Response.ok(m).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Modelo de bus no encontrado").build());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBusModel(@PathParam("id") Long id) {
        try {
            deleteBusModelUseCase.execute(id);
            return Response.ok(Map.of("message", "Modelo eliminado correctamente")).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Modelo de bus no encontrado").build();
        } catch (Exception e) {
            log.severe("Error eliminando modelo " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateBusModel(@PathParam("id") Long id, UpdateBusModelDto dto) {
        try {
            var updated = updateBusModelUseCase.execute(id, dto);
            return Response.ok(Map.of(
                    "message", "Modelo actualizado correctamente",
                    "model", updated
            )).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Modelo de bus no encontrado").build();
        } catch (Exception e) {
            log.severe("Error actualizando modelo " + id + ": " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
