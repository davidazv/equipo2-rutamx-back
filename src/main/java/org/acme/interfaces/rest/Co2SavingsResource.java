package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.application.usecase.CalculateCo2SavingsUseCase;

import java.util.logging.Logger;

@Path("/api/co2-savings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Co2SavingsResource {

    private static final Logger log = Logger.getLogger(Co2SavingsResource.class.getName());

    private final CalculateCo2SavingsUseCase calculateCo2SavingsUseCase;

    @Inject
    public Co2SavingsResource(CalculateCo2SavingsUseCase calculateCo2SavingsUseCase) {
        this.calculateCo2SavingsUseCase = calculateCo2SavingsUseCase;
    }

    @GET
    public Response getCo2Savings(@QueryParam("busModelId") Long busModelId) {
        if (busModelId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro busModelId es requerido").build();
        }

        try {
            return Response.ok(calculateCo2SavingsUseCase.execute(busModelId)).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NoGtfsDataException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error calculando ahorro CO2: " + e.getMessage());
            return Response.serverError().entity("Error inesperado al calcular ahorro de emisiones CO2").build();
        }
    }
}
