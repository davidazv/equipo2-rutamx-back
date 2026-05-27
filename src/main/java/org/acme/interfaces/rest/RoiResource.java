package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.EstimateRoiUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/roi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "ROI", description = "Estimación del retorno sobre la inversión al electrificar una flotilla")
public class RoiResource {

    private static final Logger log = Logger.getLogger(RoiResource.class.getName());

    private final EstimateRoiUseCase estimateRoiUseCase;

    @Inject
    public RoiResource(EstimateRoiUseCase estimateRoiUseCase) {
        this.estimateRoiUseCase = estimateRoiUseCase;
    }

    @GET
    @Path("/estimate")
    @Operation(summary = "Estimar ROI",
        description = "Calcula el retorno sobre la inversión (payback period, VPN) para la electrificación de una ruta con un modelo dado. Página: /ceo/report | /ceo/dashboard. **Roles:** ADMIN, CEO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Estimación de ROI calculada"),
        @APIResponse(responseCode = "400", description = "Parámetros inválidos o faltantes"),
        @APIResponse(responseCode = "404", description = "Ruta o modelo de bus no encontrado"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response estimateRoi(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @QueryParam("routeId") String routeId,
            @Parameter(description = "ID del modelo de bus eléctrico", required = true, example = "1")
            @QueryParam("modelId") Long modelId,
            @Parameter(description = "Número de buses en la flotilla", example = "10")
            @QueryParam("buses") @DefaultValue("10") int buses) {
        if (routeId == null || routeId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro routeId es requerido").build();
        }
        if (modelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro modelId es requerido").build();
        }
        if (buses < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El número de buses debe ser al menos 1").build();
        }

        try {
            return Response.ok(estimateRoiUseCase.execute(routeId, modelId, buses)).build();
        } catch (RouteNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado estimando ROI: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
