package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetCmoRouteStatsUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
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

    @Inject
    public CmoResource(GetCmoRouteStatsUseCase getCmoRouteStatsUseCase) {
        this.getCmoRouteStatsUseCase = getCmoRouteStatsUseCase;
    }

    @GET
    @Path("/route-stats")
    @Operation(summary = "Estadísticas por ruta para CMO",
        description = "Devuelve métricas de demanda, distancia y frecuencia agrupadas por ruta. Página: /cmo/dashboard > pestaña Análisis de Rutas. **Roles:** ADMIN, CMO")
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
}
