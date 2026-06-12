package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.application.usecase.CalculateCo2SavingsUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/co2-savings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Sostenibilidad", description = "Cálculo de ahorro de emisiones CO2 al reemplazar buses diésel")
public class Co2SavingsResource {

    private static final Logger log = Logger.getLogger(Co2SavingsResource.class.getName());

    private final CalculateCo2SavingsUseCase calculateCo2SavingsUseCase;

    @Inject
    public Co2SavingsResource(CalculateCo2SavingsUseCase calculateCo2SavingsUseCase) {
        this.calculateCo2SavingsUseCase = calculateCo2SavingsUseCase;
    }

    @GET
    @Operation(summary = "Calcular ahorro de emisiones CO2",
        description = "Estima el ahorro total de CO2 (toneladas) al sustituir la flotilla diésel por el modelo eléctrico especificado. Página: /admin/map | /cmo/map > pestaña Campañas Ambientales. **Roles:** ADMIN, CMO")
    @APIResponse(responseCode = "200", description = "Ahorro de CO2 calculado")
    @APIResponse(responseCode = "400", description = "Parámetro busModelId requerido o modelo no válido")
    @APIResponse(responseCode = "404", description = "No hay datos GTFS cargados")
    @APIResponse(responseCode = "500", description = "Error inesperado")
    public Response getCo2Savings(
            @Parameter(description = "ID del modelo de bus eléctrico", required = true, example = "1")
            @QueryParam("busModelId") Long busModelId) {
        if (busModelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro busModelId es requerido").build();
        }

        try {
            return Response.ok(calculateCo2SavingsUseCase.execute(busModelId)).build();
        } catch (BusModelNotFoundException e) {
            log.warning("Bus model not found: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Modelo de autobús no encontrado").build();
        } catch (IllegalArgumentException e) {
            log.warning("Invalid argument calculating CO2 savings: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetros inválidos").build();
        } catch (NoGtfsDataException e) {
            log.warning("No GTFS data: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Datos GTFS no disponibles").build();
        } catch (Exception e) {
            log.severe("Error calculando ahorro CO2: " + e.getMessage());
            return Response.serverError().entity("Error inesperado al calcular ahorro de emisiones CO2").build();
        }
    }
}
