package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.repository.BusModelRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/bus-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Modelos de Bus", description = "CRUD de modelos de autobús eléctrico y diésel")
public class BusModelResource {

    private static final Logger log = Logger.getLogger(BusModelResource.class.getName());

    private final BusModelRepository busModelRepository;

    @Inject
    public BusModelResource(BusModelRepository busModelRepository) {
        this.busModelRepository = busModelRepository;
    }

    @GET
    @Operation(summary = "Listar modelos de bus",
        description = "Página: Admin Panel > Catálogo de Buses | /ceo/fleet | /coo/fleet | /cmo/dashboard (Reporte Comparativo). **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Lista de modelos de bus")
    public Response listBusModels() {
        return Response.ok(busModelRepository.findAll()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener modelo de bus por ID",
        description = "Página: Admin Panel > Catálogo de Buses. **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Modelo encontrado")
    @APIResponse(responseCode = "404", description = "Modelo no encontrado")
    public Response getBusModel(
            @Parameter(description = "ID del modelo de bus", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        return busModelRepository.findById(id)
                .map(m -> Response.ok(m).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Modelo de bus no encontrado").build());
    }

    @POST
    @Transactional
    @Operation(summary = "Crear modelo de bus",
        description = "Registra un nuevo modelo en el catálogo. Página: Admin Panel > Catálogo de Buses. **Roles:** ADMIN")
    @APIResponse(responseCode = "201", description = "Modelo creado exitosamente")
    @APIResponse(responseCode = "409", description = "Ya existe un modelo con ese nombre")
    @RequestBody(description = "Datos del modelo de bus", required = true,
        content = @Content(schema = @Schema(implementation = BusModel.class)))
    public Response createBusModel(@Valid BusModel model) {
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
    @Operation(summary = "Actualizar modelo de bus",
        description = "Página: Admin Panel > Catálogo de Buses. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Modelo actualizado")
    @APIResponse(responseCode = "404", description = "Modelo no encontrado")
    @APIResponse(responseCode = "409", description = "Conflicto de nombre duplicado")
    @RequestBody(description = "Datos actualizados del modelo", required = true,
        content = @Content(schema = @Schema(implementation = BusModel.class)))
    public Response updateBusModel(
            @Parameter(description = "ID del modelo de bus", required = true, example = "1")
            @PathParam("id") @Positive Long id,
            @Valid BusModel model) {
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
    @Operation(summary = "Eliminar modelo de bus",
        description = "Página: Admin Panel > Catálogo de Buses. **Roles:** ADMIN")
    @APIResponse(responseCode = "204", description = "Modelo eliminado")
    @APIResponse(responseCode = "404", description = "Modelo no encontrado")
    public Response deleteBusModel(
            @Parameter(description = "ID del modelo de bus", required = true, example = "1")
            @PathParam("id") @Positive Long id) {
        try {
            busModelRepository.delete(id);
            return Response.noContent().build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("MODEL_NOT_FOUND").build();
        }
    }
}
