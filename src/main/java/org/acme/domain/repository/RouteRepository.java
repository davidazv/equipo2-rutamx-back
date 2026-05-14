package org.acme.domain.repository;

import org.acme.domain.models.Route;
import org.acme.domain.models.RouteGeometry;
import org.acme.domain.models.RouteTimeComparison;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    List<Route> findAllWithDistance();
    Optional<Route> findByIdWithDistance(String routeId);
    Optional<Route> findByAgencyAndShortName(String agencyId, String routeShortName);
    List<RouteGeometry> findAllWithShapes();
    List<RouteGeometry> findByAgencyWithShapes(String agencyId);
    /** Returns per-route scheduled time (from GTFS stop_times) and frequency (from frequencies). */
    List<RouteTimeComparison> findAllWithTimeComparison();
    /** Returns time comparison data for a single route. Used by HU12. */
    Optional<RouteTimeComparison> findByIdWithTimeComparison(String routeId);
    void deleteAll();
    int createAll(List<Route> items);
}
