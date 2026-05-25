package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetHourlyFrequencyStatsUseCase;
import org.acme.application.usecase.GetKpiMetricsUseCase;
import org.acme.application.usecase.GetOperationalSummaryUseCase;
import org.acme.application.usecase.GetPassengerTrendUseCase;

import java.util.logging.Logger;

@Path("/api/kpi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KpiResource {

    private static final Logger log = Logger.getLogger(KpiResource.class.getName());

    private final GetKpiMetricsUseCase getKpiMetricsUseCase;
    private final GetOperationalSummaryUseCase getOperationalSummaryUseCase;
    private final GetPassengerTrendUseCase getPassengerTrendUseCase;
    private final GetHourlyFrequencyStatsUseCase getHourlyFrequencyStatsUseCase;

    @Inject
    public KpiResource(GetKpiMetricsUseCase getKpiMetricsUseCase,
                       GetOperationalSummaryUseCase getOperationalSummaryUseCase,
                       GetPassengerTrendUseCase getPassengerTrendUseCase,
                       GetHourlyFrequencyStatsUseCase getHourlyFrequencyStatsUseCase) {
        this.getKpiMetricsUseCase = getKpiMetricsUseCase;
        this.getOperationalSummaryUseCase = getOperationalSummaryUseCase;
        this.getPassengerTrendUseCase = getPassengerTrendUseCase;
        this.getHourlyFrequencyStatsUseCase = getHourlyFrequencyStatsUseCase;
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

    @GET
    @Path("/operational-summary")
    public Response getOperationalSummary() {
        try {
            return Response.ok(getOperationalSummaryUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error obteniendo operational summary: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @GET
    @Path("/passenger-trend")
    public Response getPassengerTrend() {
        try {
            return Response.ok(getPassengerTrendUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error obteniendo passenger trend: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @GET
    @Path("/hourly-stats")
    public Response getHourlyStats() {
        try {
            return Response.ok(getHourlyFrequencyStatsUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error obteniendo hourly stats: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
