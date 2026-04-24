package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetKpiMetricsUseCase;

import java.util.logging.Logger;

@Path("/api/kpi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KpiResource {

    private static final Logger log = Logger.getLogger(KpiResource.class.getName());

    private final GetKpiMetricsUseCase getKpiMetricsUseCase;

    @Inject
    public KpiResource(GetKpiMetricsUseCase getKpiMetricsUseCase) {
        this.getKpiMetricsUseCase = getKpiMetricsUseCase;
    }

    @GET
    @Path("/summary")
    public Response getSummary(@QueryParam("busesPerRoute") @DefaultValue("10") int busesPerRoute) {
        if (busesPerRoute < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("busesPerRoute debe ser al menos 1").build();
        }

        try {
            return Response.ok(getKpiMetricsUseCase.execute(busesPerRoute)).build();
        } catch (Exception e) {
            log.severe("Error obteniendo KPI: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
