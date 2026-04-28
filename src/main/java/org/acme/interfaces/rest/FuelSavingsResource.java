package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.CalculateFuelSavingsUseCase;

import java.util.logging.Logger;

@Path("/api/fuel-savings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FuelSavingsResource {

    private static final Logger log = Logger.getLogger(FuelSavingsResource.class.getName());

    private final CalculateFuelSavingsUseCase calculateFuelSavingsUseCase;

    @Inject
    public FuelSavingsResource(CalculateFuelSavingsUseCase calculateFuelSavingsUseCase) {
        this.calculateFuelSavingsUseCase = calculateFuelSavingsUseCase;
    }

    @GET
    public Response getFuelSavings(@QueryParam("routeId") String routeId,
                                   @QueryParam("modelId") Long modelId,
                                   @QueryParam("buses") @DefaultValue("10") int buses,
                                   @QueryParam("years") @DefaultValue("5") int years) {
        if (routeId == null || routeId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro routeId es requerido").build();
        }
        if (modelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro modelId es requerido").build();
        }
        if (buses < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El número de buses debe ser al menos 1").build();
        }
        if (years < 1 || years > 10) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El período de proyección debe ser entre 1 y 10 años").build();
        }

        try {
            return Response.ok(calculateFuelSavingsUseCase.execute(routeId, modelId, buses, years)).build();
        } catch (RouteNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado calculando ahorro en combustible: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
