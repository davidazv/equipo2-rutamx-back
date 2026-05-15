package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetCmoRouteStatsUseCase;

import java.util.logging.Logger;

@Path("/api/cmo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CmoResource {

    private static final Logger log = Logger.getLogger(CmoResource.class.getName());

    private final GetCmoRouteStatsUseCase getCmoRouteStatsUseCase;

    @Inject
    public CmoResource(GetCmoRouteStatsUseCase getCmoRouteStatsUseCase) {
        this.getCmoRouteStatsUseCase = getCmoRouteStatsUseCase;
    }

    @GET
    @Path("/route-stats")
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
