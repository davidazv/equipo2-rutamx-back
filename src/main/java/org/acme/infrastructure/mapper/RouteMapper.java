package org.acme.infrastructure.mapper;

import org.acme.domain.models.Route;
import org.acme.infrastructure.entities.RouteEntity;
import org.acme.infrastructure.entities.AgencyEntity;

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

    public static Route toDomain(RouteEntity entity) {
        Route route = new Route();
        route.setRouteId(entity.getRouteId());
        route.setAgencyId(entity.getAgency() != null ? entity.getAgency().getAgencyId() : null);
        route.setRouteShortName(entity.getRouteShortName());
        route.setRouteLongName(entity.getRouteLongName());
        route.setRouteType(entity.getRouteType());
        route.setRouteColor(entity.getRouteColor());
        route.setRouteTextColor(entity.getRouteTextColor());
        return route;
    }

    public static RouteEntity toEntity(Route route) {
        RouteEntity entity = new RouteEntity();
        entity.setRouteId(route.getRouteId());
        entity.setRouteShortName(route.getRouteShortName());
        entity.setRouteLongName(route.getRouteLongName());
        entity.setRouteType(route.getRouteType());
        entity.setRouteColor(route.getRouteColor());
        entity.setRouteTextColor(route.getRouteTextColor());
        // agency association wired by repository
        return entity;
    }
}
