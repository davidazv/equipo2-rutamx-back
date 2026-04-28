package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.DemandNotFoundException;
import org.acme.application.usecase.RecommendBusCountUseCase;
import org.acme.application.usecase.RecommendBusModelUseCase;

import java.util.logging.Logger;

@Path("/api/fleet")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FleetResource {

    private static final Logger log = Logger.getLogger(FleetResource.class.getName());

    private final RecommendBusCountUseCase recommendBusCountUseCase;
    private final RecommendBusModelUseCase recommendBusModelUseCase;

    @Inject
    public FleetResource(RecommendBusCountUseCase recommendBusCountUseCase,
                         RecommendBusModelUseCase recommendBusModelUseCase) {
        this.recommendBusCountUseCase = recommendBusCountUseCase;
        this.recommendBusModelUseCase = recommendBusModelUseCase;
    }

    @GET
    @Path("/bus-count")
    public Response getBusCount(@QueryParam("linea") String linea,
                                @QueryParam("dayType") String dayType,
                                @QueryParam("occupancy") Integer occupancy) {
        if (linea == null || linea.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro linea es requerido").build();
        }
        if (occupancy != null && (occupancy < 60 || occupancy > 95)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La ocupación debe estar entre 60 y 95").build();
        }
        try {
            return Response.ok(recommendBusCountUseCase.execute(linea, dayType, occupancy)).build();
        } catch (DemandNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error calculando bus count: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }

    @GET
    @Path("/model-recommendation")
    public Response getModelRecommendation(@QueryParam("linea") String linea,
                                            @QueryParam("dayType") String dayType,
                                            @QueryParam("occupancy") Integer occupancy) {
        if (linea == null || linea.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parámetro linea es requerido").build();
        }
        if (occupancy != null && (occupancy < 60 || occupancy > 95)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La ocupación debe estar entre 60 y 95").build();
        }
        try {
            return Response.ok(recommendBusModelUseCase.execute(linea, dayType, occupancy)).build();
        } catch (DemandNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error recomendando modelo: " + e.getMessage());
            return Response.serverError().entity("Error inesperado").build();
        }
    }
}
