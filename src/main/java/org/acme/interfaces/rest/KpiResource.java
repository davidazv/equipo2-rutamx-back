package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.GetHourlyFrequencyStatsUseCase;
import org.acme.application.usecase.GetKpiMetricsUseCase;
import org.acme.application.usecase.GetOperationalSummaryUseCase;
import org.acme.application.usecase.GetPassengerTrendUseCase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/kpi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "KPIs", description = "Indicadores clave de desempeño de la flotilla")
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
    @Operation(summary = "Resumen de KPIs",
        description = "Calcula métricas clave: autonomía, consumo y eficiencia por número de buses. Página: /admin/dashboard | /ceo/dashboard | /coo/dashboard. **Roles:** ADMIN, CEO, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Métricas KPI calculadas"),
        @APIResponse(responseCode = "400", description = "busesPerRoute debe ser al menos 1"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response getSummary(
            @Parameter(description = "Número de buses por ruta para el cálculo", example = "10")
            @QueryParam("busesPerRoute") @DefaultValue("10") int busesPerRoute) {
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
    @Operation(summary = "Resumen operacional",
        description = "Devuelve totales operacionales: rutas activas, viajes, paradas. Página: /admin/dashboard | /ceo/dashboard | /coo/dashboard. **Roles:** ADMIN, CEO, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Resumen operacional"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
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
    @Operation(summary = "Tendencia de pasajeros",
        description = "Devuelve la afluencia de pasajeros agrupada por período. Página: /admin/dashboard | /ceo/dashboard | /coo/dashboard. **Roles:** ADMIN, CEO, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Tendencia de pasajeros"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
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
    @Operation(summary = "Estadísticas de frecuencia por hora",
        description = "Devuelve distribución horaria de frecuencias de viaje. Página: /admin/dashboard | /coo/dashboard. **Roles:** ADMIN, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Estadísticas horarias"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response getHourlyStats() {
        try {
            return Response.ok(getHourlyFrequencyStatsUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error obteniendo hourly stats: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
