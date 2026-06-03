package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetCmoDashboardUseCase;
import org.acme.application.usecase.GetCmoRouteStatsUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/cmo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "CMO", description = "Estadísticas agregadas por ruta para el CMO")
public class CmoResource {

    private static final Logger log = Logger.getLogger(CmoResource.class.getName());

    private final GetCmoRouteStatsUseCase getCmoRouteStatsUseCase;
    private final GetCmoDashboardUseCase getCmoDashboardUseCase;

    @Inject
    public CmoResource(GetCmoRouteStatsUseCase getCmoRouteStatsUseCase,
                       GetCmoDashboardUseCase getCmoDashboardUseCase) {
        this.getCmoRouteStatsUseCase = getCmoRouteStatsUseCase;
        this.getCmoDashboardUseCase = getCmoDashboardUseCase;
    }

    @GET
    @Path("/route-stats")
    @Deprecated
    @Operation(summary = "Estadísticas por ruta para CMO (deprecated)",
        description = "Sustituido por /api/cmo/dashboard que regresa rutas + agencias en una sola llamada. **Roles:** ADMIN, CMO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Estadísticas por ruta"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response getRouteStats() {
        try {
            return Response.ok(getCmoRouteStatsUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error obteniendo estadísticas por ruta para CMO: " + e.getMessage());
            return Response.serverError()
                    .entity("Error inesperado al obtener estadísticas por ruta")
                    .build();
        }
    }

    @GET
    @Path("/dashboard")
    @Operation(summary = "Dashboard consolidado del CMO",
        description = "Una sola llamada que regresa rutas con métricas (distancia, viajes/día, headway, CO₂ diesel y eléctrico) más agencias con sus IDs. Implementado vía sp_get_cmo_dashboard. Página: /cmo/dashboard. **Roles:** ADMIN, CMO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Dashboard CMO consolidado"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response getDashboard(
            @Parameter(description = "Filtro opcional por agency_id") @QueryParam("agencyId") String agencyId) {
        try {
            return Response.ok(getCmoDashboardUseCase.execute(agencyId)).build();
        } catch (Exception e) {
            log.severe("Error obteniendo dashboard CMO: " + e.getMessage());
            return Response.serverError()
                    .entity("Error inesperado al obtener dashboard CMO")
                    .build();
        }
    }
}
