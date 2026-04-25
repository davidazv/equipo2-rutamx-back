package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.domain.models.Route;
import org.acme.domain.repository.RouteRepository;
import org.acme.infrastructure.mapper.RouteMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RouteRepositoryImpl implements RouteRepository {

    private static final String ROUTES_WITH_DISTANCE_QUERY =
            "SELECT r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type, " +
            "MAX(s.shape_dist_traveled) AS distance_km " +
            "FROM routes r " +
            "INNER JOIN trips t ON t.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = t.shape_id " +
            "GROUP BY r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type " +
            "HAVING MAX(s.shape_dist_traveled) > 0 " +
            "ORDER BY r.route_short_name";

    private static final String ROUTE_BY_ID_WITH_DISTANCE_QUERY =
            "SELECT r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type, " +
            "MAX(s.shape_dist_traveled) AS distance_km " +
            "FROM routes r " +
            "INNER JOIN trips t ON t.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = t.shape_id " +
            "WHERE r.route_id = ?1 " +
            "GROUP BY r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type " +
            "HAVING MAX(s.shape_dist_traveled) > 0";

    @Inject
    EntityManager entityManager;

    @Override
    public List<Route> findAllWithDistance() {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTES_WITH_DISTANCE_QUERY)
                .getResultList();

        List<Route> routes = new ArrayList<>();
        for (Object[] row : results) {
            routes.add(mapRow(row));
        }
        return routes;
    }

    @Override
    public Optional<Route> findByIdWithDistance(String routeId) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTE_BY_ID_WITH_DISTANCE_QUERY)
                .setParameter(1, routeId)
                .getResultList();

        return results.stream().findFirst().map(this::mapRow);
    }

    private Route mapRow(Object[] row) {
        String routeId = (String) row[0];
        String agencyId = (String) row[1];
        String shortName = (String) row[2];
        String longName = (String) row[3];
        Integer routeType = ((Number) row[4]).intValue();
        Double distanceKm = row[5] instanceof BigDecimal
                ? ((BigDecimal) row[5]).doubleValue()
                : ((Number) row[5]).doubleValue();

        return RouteMapper.toDomain(routeId, agencyId, shortName, longName, routeType, distanceKm);
    }
}
