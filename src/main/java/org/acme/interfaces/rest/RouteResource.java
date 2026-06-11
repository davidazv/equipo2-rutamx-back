package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.exception.NoDemandDataException;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.application.usecase.GetRouteTravelTimesUseCase;
import org.acme.application.usecase.GetTripsByDayUseCase;
import org.acme.application.usecase.RecommendBusModelUseCase;
import org.acme.domain.repository.RouteRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/api/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Rutas", description = "Consulta de rutas GTFS y análisis de tiempos de viaje")
public class RouteResource {

    private static final Logger log = Logger.getLogger(RouteResource.class.getName());

    private final RouteRepository routeRepository;
    private final GetRouteTravelTimesUseCase getTravelTimesUseCase;
    private final GetTripsByDayUseCase getTripsByDayUseCase;
    private final RecommendBusModelUseCase recommendBusModelUseCase;

    @Inject
    public RouteResource(RouteRepository routeRepository,
                         GetRouteTravelTimesUseCase getTravelTimesUseCase,
                         GetTripsByDayUseCase getTripsByDayUseCase,
                         RecommendBusModelUseCase recommendBusModelUseCase) {
        this.routeRepository = routeRepository;
        this.getTravelTimesUseCase = getTravelTimesUseCase;
        this.getTripsByDayUseCase = getTripsByDayUseCase;
        this.recommendBusModelUseCase = recommendBusModelUseCase;
    }

    @GET
    @Operation(summary = "Listar rutas con distancia",
        description = "Devuelve todas las rutas con su distancia total calculada. Página: /[rol]/map | /coo/dashboard | /ceo/report | /cmo/dashboard. **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Lista de rutas")
    public Response listRoutes() {
        return Response.ok(routeRepository.findAllWithDistance()).build();
    }

    @GET
    @Path("/shapes")
    @Operation(summary = "Listar rutas con trazado geográfico",
        description = "Devuelve rutas incluyendo los shapes (polilíneas). Filtrable por agencia. Página: /[rol]/map (mapa interactivo). **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponse(responseCode = "200", description = "Lista de rutas con shapes")
    public Response listRoutesWithShapes(
            @Parameter(description = "ID de la agencia para filtrar (opcional)", example = "MB")
            @QueryParam("agencyId") @Size(max = 50)
            @Pattern(regexp = "^\\s*$|^[A-Za-z0-9_-]{1,50}$", message = "agencyId inválido") String agencyId) {
        if (agencyId != null && !agencyId.isBlank()) {
            return Response.ok(routeRepository.findByAgencyWithShapes(agencyId)).build();
        }
        return Response.ok(routeRepository.findAllWithShapes()).build();
    }

    @GET
    @Path("/travel-times")
    @Operation(summary = "Tiempos de recorrido por ruta",
        description = "Calcula el tiempo promedio de viaje para cada ruta. Página: /admin/fleet | /coo/fleet > pestaña Tiempos de Viaje. **Roles:** ADMIN, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Tiempos de recorrido por ruta"),
        @APIResponse(responseCode = "500", description = "Error al calcular tiempos")
    })
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
    @Operation(summary = "Viajes por día de la semana",
        description = "Devuelve el número de viajes agrupados por día según el calendario GTFS. Página: /admin/dashboard | /ceo/dashboard | /coo/dashboard. **Roles:** ADMIN, CEO, COO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Viajes agrupados por día"),
        @APIResponse(responseCode = "404", description = "No hay datos GTFS cargados"),
        @APIResponse(responseCode = "500", description = "Error inesperado")
    })
    public Response listTripsByDay() {
        log.info("GET /api/routes/trips-by-day");
        try {
            return Response.ok(getTripsByDayUseCase.execute()).build();
        } catch (NoGtfsDataException e) {
            log.warning("No GTFS data: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Datos GTFS no disponibles").build();
        } catch (Exception e) {
            log.severe("Error listing trips by day: " + e.getMessage());
            return Response.serverError().entity("Error inesperado al obtener viajes por día").build();
        }
    }

    @GET
    @Path("/{routeId}")
    @Operation(summary = "Obtener ruta por ID",
        description = "Página: detalle de ruta en mapa (/[rol]/map). **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Ruta encontrada"),
        @APIResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    public Response getRoute(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @PathParam("routeId") @Size(max = 50)
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "routeId inválido") String routeId) {
        return routeRepository.findByIdWithDistance(routeId)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Ruta no encontrada").build());
    }

    @GET
    @Path("/{routeId}/bus-model-recommendation")
    @Operation(summary = "Recomendación de modelo de bus para una ruta",
        description = "Calcula el modelo de bus eléctrico más adecuado según distancia y ocupación objetivo. Página: /[rol]/map > pestaña Optimización de Flota. **Roles:** ADMIN, CEO, COO, CMO")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Recomendación generada"),
        @APIResponse(responseCode = "404", description = "Ruta no encontrada o sin datos de demanda"),
        @APIResponse(responseCode = "500", description = "Error al generar recomendación")
    })
    public Response getBusModelRecommendation(
            @Parameter(description = "ID de la ruta (GTFS route_id)", required = true, example = "MB-1")
            @PathParam("routeId") @Size(max = 50)
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "routeId inválido") String routeId,
            @Parameter(description = "Ocupación objetivo como fracción (0.0–1.0)", example = "0.80")
            @QueryParam("targetOccupancy") @DefaultValue("0.80")
            @DecimalMin("0.0") @DecimalMax("1.0") double targetOccupancy) {
        log.info("GET /api/routes/" + routeId + "/bus-model-recommendation"
                + " targetOccupancy=" + targetOccupancy);
        try {
            return Response.ok(recommendBusModelUseCase.execute(routeId, targetOccupancy)).build();
        } catch (RouteNotFoundException e) {
            log.warning("Route not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Ruta no encontrada").build();
        } catch (NoDemandDataException e) {
            log.warning("No demand data: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Sin datos de demanda para la ruta").build();
        } catch (Exception e) {
            log.severe("Error generating bus model recommendation: " + e.getMessage());
            return Response.serverError().entity("Error al generar recomendación de modelo").build();
        }
    }
}
