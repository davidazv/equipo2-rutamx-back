package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.CalculateEnergyConsumptionUseCase;

import java.util.logging.Logger;

@Path("/api/energy-consumption")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnergyConsumptionResource {

    private static final Logger log = Logger.getLogger(EnergyConsumptionResource.class.getName());

    private final CalculateEnergyConsumptionUseCase calculateEnergyConsumptionUseCase;

    @Inject
    public EnergyConsumptionResource(CalculateEnergyConsumptionUseCase calculateEnergyConsumptionUseCase) {
        this.calculateEnergyConsumptionUseCase = calculateEnergyConsumptionUseCase;
    }

    @GET
    public Response calculateEnergyConsumption(@QueryParam("routeId") String routeId,
                                               @QueryParam("busModelId") Long busModelId,
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
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado calculando consumo energético: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
