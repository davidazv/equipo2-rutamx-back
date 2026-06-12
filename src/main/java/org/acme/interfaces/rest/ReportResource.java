package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.GenerateComparativeReportUseCase;
import org.acme.infrastructure.security.AuthContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Reportes", description = "Generación de reportes comparativos")
public class ReportResource {

    private static final Logger log = Logger.getLogger(ReportResource.class.getName());

    private final GenerateComparativeReportUseCase generateComparativeReportUseCase;
    private final AuthContext authContext;

    @Inject
    public ReportResource(GenerateComparativeReportUseCase generateComparativeReportUseCase,
                          AuthContext authContext) {
        this.generateComparativeReportUseCase = generateComparativeReportUseCase;
        this.authContext = authContext;
    }

    @GET
    @Path("/comparative")
    @Operation(summary = "Reporte comparativo eléctrico vs diésel",
        description = "Genera un análisis comparativo financiero y ambiental entre un modelo eléctrico y uno diésel para una ruta y período dados. Página: /ceo/report | /cmo/dashboard > pestaña Reporte Comparativo. **Roles:** ADMIN, CEO, CMO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Reporte comparativo generado"),
        @APIResponse(responseCode = "400", description = "Parámetros inválidos o faltantes"),
        @APIResponse(responseCode = "403", description = "Solo el rol CMO puede acceder a este reporte"),
        @APIResponse(responseCode = "404", description = "Ruta o modelo de bus no encontrado"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response generateComparativeReport(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @QueryParam("routeId") @Size(max = 50)
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "routeId inválido") String routeId,
            @Parameter(description = "ID del modelo de bus eléctrico", required = true, example = "1")
            @QueryParam("electricModelId") @Positive Long electricModelId,
            @Parameter(description = "ID del modelo de bus diésel", required = true, example = "2")
            @QueryParam("dieselModelId") @Positive Long dieselModelId,
            @Parameter(description = "Número de buses en la flotilla", example = "10")
            @QueryParam("buses") @DefaultValue("10")
            @Min(value = 1, message = "El número de buses debe ser al menos 1")
            @Max(value = 10000, message = "El número de buses no puede superar 10000") int buses,
            @Parameter(description = "Años de proyección financiera (1–30)", example = "10")
            @QueryParam("years") @DefaultValue("10")
            @Min(value = 1, message = "Los años de proyección deben estar entre 1 y 30")
            @Max(value = 30, message = "Los años de proyección deben estar entre 1 y 30") int years) {

        if (authContext.getUser() == null || !"CMO".equals(authContext.getUser().getRoleName())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Solo el rol CMO puede acceder a este reporte").build();
        }

        if (routeId == null || routeId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro routeId es requerido").build();
        }
        if (electricModelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro electricModelId es requerido").build();
        }
        if (dieselModelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro dieselModelId es requerido").build();
        }
        if (buses < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El número de buses debe ser al menos 1").build();
        }
        if (years < 1 || years > 30) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los años de proyección deben estar entre 1 y 30").build();
        }

        try {
            return Response.ok(generateComparativeReportUseCase.execute(
                    routeId, electricModelId, dieselModelId, buses, years)).build();
        } catch (RouteNotFoundException e) {
            log.warning("Route not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Ruta no encontrada").build();
        } catch (BusModelNotFoundException e) {
            log.warning("Bus model not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Modelo de autobús no encontrado").build();
        } catch (IllegalArgumentException e) {
            log.warning("Invalid argument generating comparative report: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado generando reporte comparativo: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
