package org.acme.domain.repository;

import org.acme.domain.models.Route;
import org.acme.domain.models.RouteGeometry;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    List<Route> findAllWithDistance();
    Optional<Route> findByIdWithDistance(String routeId);
    Optional<Route> findByAgencyAndShortName(String agencyId, String routeShortName);
    List<RouteGeometry> findAllWithShapes();
    List<RouteGeometry> findByAgencyWithShapes(String agencyId);
    void deleteAll();
    int createAll(List<Route> items);
}
