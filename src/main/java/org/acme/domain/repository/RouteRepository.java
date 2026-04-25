package org.acme.domain.repository;

import org.acme.domain.models.Route;
import org.acme.domain.models.RouteGeometry;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    List<Route> findAllWithDistance();
    Optional<Route> findByIdWithDistance(String routeId);
    List<RouteGeometry> findAllWithShapes();
    List<RouteGeometry> findByAgencyWithShapes(String agencyId);
}
