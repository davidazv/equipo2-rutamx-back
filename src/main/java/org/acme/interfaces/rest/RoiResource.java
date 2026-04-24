package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.EstimateRoiUseCase;

import java.util.logging.Logger;

@Path("/api/roi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoiResource {

    private static final Logger log = Logger.getLogger(RoiResource.class.getName());

    private final EstimateRoiUseCase estimateRoiUseCase;

    @Inject
    public RoiResource(EstimateRoiUseCase estimateRoiUseCase) {
        this.estimateRoiUseCase = estimateRoiUseCase;
    }

    @GET
    @Path("/estimate")
    public Response estimateRoi(@QueryParam("routeId") String routeId,
                                @QueryParam("modelId") Long modelId,
                                @QueryParam("buses") @DefaultValue("10") int buses) {
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

        try {
            return Response.ok(estimateRoiUseCase.execute(routeId, modelId, buses)).build();
        } catch (RouteNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BusModelNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error inesperado estimando ROI: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
