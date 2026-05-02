package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.application.usecase.GetRouteTravelTimesUseCase;
import org.acme.application.usecase.GetTripsByDayUseCase;
import org.acme.domain.repository.RouteRepository;

import java.util.logging.Logger;

@Path("/api/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    private static final Logger log = Logger.getLogger(RouteResource.class.getName());

    private final RouteRepository routeRepository;
    private final GetRouteTravelTimesUseCase getTravelTimesUseCase;
    private final GetTripsByDayUseCase getTripsByDayUseCase;

    @Inject
    public RouteResource(RouteRepository routeRepository,
                         GetRouteTravelTimesUseCase getTravelTimesUseCase,
                         GetTripsByDayUseCase getTripsByDayUseCase) {
        this.routeRepository = routeRepository;
        this.getTravelTimesUseCase = getTravelTimesUseCase;
        this.getTripsByDayUseCase = getTripsByDayUseCase;
    }

    @GET
    public Response listRoutes() {
        return Response.ok(routeRepository.findAllWithDistance()).build();
    }

    @GET
    @Path("/shapes")
    public Response listRoutesWithShapes(@QueryParam("agencyId") String agencyId) {
        if (agencyId != null && !agencyId.isBlank()) {
            return Response.ok(routeRepository.findByAgencyWithShapes(agencyId)).build();
        }
        return Response.ok(routeRepository.findAllWithShapes()).build();
    }

    @GET
    @Path("/travel-times")
    public Response listRouteTravelTimes() {
        log.info("GET /api/routes/travel-times");
        try {
            return Response.ok(getTravelTimesUseCase.execute()).build();
        } catch (Exception e) {
            log.severe("Error listing travel times: " + e.getMessage());
            return Response.serverError().entity("Error al obtener tiempos de recorrido").build();
        }
    }

    @GET
    @Path("/trips-by-day")
    public Response listTripsByDay() {
        log.info("GET /api/routes/trips-by-day");
        try {
            return Response.ok(getTripsByDayUseCase.execute()).build();
        } catch (NoGtfsDataException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.severe("Error listing trips by day: " + e.getMessage());
            return Response.serverError().entity("Error inesperado al obtener viajes por día").build();
        }
    }

    @GET
    @Path("/{routeId}")
    public Response getRoute(@PathParam("routeId") String routeId) {
        return routeRepository.findByIdWithDistance(routeId)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Ruta no encontrada").build());
    }
}
