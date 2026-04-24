package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.repository.RouteRepository;

import java.util.logging.Logger;

@Path("/api/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouteResource {

    private static final Logger log = Logger.getLogger(RouteResource.class.getName());

    private final RouteRepository routeRepository;

    @Inject
    public RouteResource(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GET
    public Response listRoutes() {
        return Response.ok(routeRepository.findAllWithDistance()).build();
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
