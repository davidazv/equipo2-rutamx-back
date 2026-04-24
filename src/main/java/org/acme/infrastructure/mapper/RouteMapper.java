package org.acme.infrastructure.mapper;

import org.acme.domain.models.Route;

public class RouteMapper {

    private RouteMapper() {}

    public static Route toDomain(String routeId, String agencyId, String routeShortName,
                                  String routeLongName, Integer routeType, Double distanceKm) {
        Route route = new Route();
        route.setRouteId(routeId);
        route.setAgencyId(agencyId);
        route.setRouteShortName(routeShortName);
        route.setRouteLongName(routeLongName);
        route.setRouteType(routeType);
        route.setDistanceKm(distanceKm);
        return route;
    }
}
