package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.CalculateEnergyConsumptionUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/energy-consumption")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Energía", description = "Cálculo de consumo energético por ruta y modelo de bus")
public class EnergyConsumptionResource {

    private static final Logger log = Logger.getLogger(EnergyConsumptionResource.class.getName());

    private final CalculateEnergyConsumptionUseCase calculateEnergyConsumptionUseCase;

    @Inject
    public EnergyConsumptionResource(CalculateEnergyConsumptionUseCase calculateEnergyConsumptionUseCase) {
        this.calculateEnergyConsumptionUseCase = calculateEnergyConsumptionUseCase;
    }

    @GET
    @Operation(summary = "Calcular consumo energético",
        description = "Estima el consumo total de energía (kWh) para una ruta con un modelo de bus y nivel de ocupación dados. Página: /admin/fleet | /ceo/fleet > pestaña Fleet Analytics. **Roles:** ADMIN, CEO, COO")
    @APIResponse(responseCode = "200", description = "Consumo energético calculado")
    @APIResponse(responseCode = "400", description = "Parámetros inválidos o faltantes")
    @APIResponse(responseCode = "404", description = "Ruta o modelo de bus no encontrado")
    @APIResponse(responseCode = "500", description = "Error inesperado")
    public Response calculateEnergyConsumption(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @QueryParam("routeId") String routeId,
            @Parameter(description = "ID del modelo de bus", required = true, example = "1")
            @QueryParam("busModelId") Long busModelId,
            @Parameter(description = "Porcentaje de ocupación (0–100)", example = "50")
            @QueryParam("occupancyPercent") @DefaultValue("50") int occupancyPercent) {
        if (routeId == null || routeId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro routeId es requerido").build();
        }
        if (busModelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro busModelId es requerido").build();
        }
        if (occupancyPercent < 0 || occupancyPercent > 100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El porcentaje de ocupación debe estar entre 0 y 100").build();
        }

        try {
            return Response.ok(calculateEnergyConsumptionUseCase.execute(routeId, busModelId, occupancyPercent)).build();
        } catch (RouteNotFoundException e) {
            log.warning("Route not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Ruta no encontrada").build();
        } catch (BusModelNotFoundException e) {
            log.warning("Bus model not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Modelo de autobús no encontrado").build();
        } catch (IllegalArgumentException e) {
            log.warning("Invalid argument calculating energy consumption: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetros inválidos").build();
        } catch (Exception e) {
            log.severe("Error inesperado calculando consumo energético: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
