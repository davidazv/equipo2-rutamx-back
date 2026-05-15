package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.GenerateComparativeReportUseCase;
import org.acme.infrastructure.security.AuthContext;

import java.util.logging.Logger;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
    public Response generateComparativeReport(
            @QueryParam("routeId") String routeId,
            @QueryParam("electricModelId") Long electricModelId,
            @QueryParam("dieselModelId") Long dieselModelId,
            @QueryParam("buses") @DefaultValue("10") int buses,
            @QueryParam("years") @DefaultValue("10") int years) {

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
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado generando reporte comparativo: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
