package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.CalculateFuelSavingsUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/fuel-savings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ahorro de Combustible", description = "Proyección del ahorro en combustible al electrificar una ruta")
public class FuelSavingsResource {

    private static final Logger log = Logger.getLogger(FuelSavingsResource.class.getName());

    private final CalculateFuelSavingsUseCase calculateFuelSavingsUseCase;

    @Inject
    public FuelSavingsResource(CalculateFuelSavingsUseCase calculateFuelSavingsUseCase) {
        this.calculateFuelSavingsUseCase = calculateFuelSavingsUseCase;
    }

    @GET
    @Operation(summary = "Calcular ahorro en combustible",
        description = "Proyecta el ahorro económico y en litros de combustible al sustituir buses diésel por el modelo eléctrico especificado. Página: /admin/fleet | /ceo/fleet > pestaña Fleet Analytics. **Roles:** ADMIN, CEO, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Proyección de ahorro calculada"),
        @APIResponse(responseCode = "400", description = "Parámetros inválidos o faltantes"),
        @APIResponse(responseCode = "404", description = "Ruta o modelo de bus no encontrado"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response getFuelSavings(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @QueryParam("routeId") String routeId,
            @Parameter(description = "ID del modelo de bus eléctrico", required = true, example = "1")
            @QueryParam("modelId") Long modelId,
            @Parameter(description = "Número de buses en la flotilla", example = "10")
            @QueryParam("buses") @DefaultValue("10") int buses,
            @Parameter(description = "Período de proyección en años (1–10)", example = "5")
            @QueryParam("years") @DefaultValue("5") int years) {
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
        if (years < 1 || years > 10) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El período de proyección debe ser entre 1 y 10 años").build();
        }

        try {
            return Response.ok(calculateFuelSavingsUseCase.execute(routeId, modelId, buses, years)).build();
        } catch (RouteNotFoundException e) {
            log.warning("Route not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Ruta no encontrada").build();
        } catch (BusModelNotFoundException e) {
            log.warning("Bus model not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Modelo de autobús no encontrado").build();
        } catch (IllegalArgumentException e) {
            log.warning("Invalid argument calculating fuel savings: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetros inválidos").build();
        } catch (Exception e) {
            log.severe("Error inesperado calculando ahorro en combustible: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
