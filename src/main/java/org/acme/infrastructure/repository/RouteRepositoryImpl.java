package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Route;
import org.acme.domain.models.RouteGeometry;
import org.acme.domain.models.RouteTimeComparison;
import org.acme.domain.repository.RouteRepository;
import org.acme.infrastructure.entities.AgencyEntity;
import org.acme.infrastructure.entities.RouteEntity;
import org.acme.infrastructure.mapper.RouteMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final String ROUTES_WITH_SHAPES_QUERY =
            "SELECT r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type, " +
            "r.route_color, s.shape_pt_lon, s.shape_pt_lat, s.shape_dist_traveled " +
            "FROM routes r " +
            "INNER JOIN (" +
            "  SELECT route_id, MIN(shape_id) AS shape_id FROM trips WHERE shape_id IS NOT NULL GROUP BY route_id" +
            ") ts ON ts.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = ts.shape_id " +
            "ORDER BY r.route_id, s.shape_pt_sequence";

    private static final String ROUTES_WITH_SHAPES_BY_AGENCY_QUERY =
            "SELECT r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type, " +
            "r.route_color, s.shape_pt_lon, s.shape_pt_lat, s.shape_dist_traveled " +
            "FROM routes r " +
            "INNER JOIN (" +
            "  SELECT route_id, MIN(shape_id) AS shape_id FROM trips WHERE shape_id IS NOT NULL GROUP BY route_id" +
            ") ts ON ts.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = ts.shape_id " +
            "WHERE r.agency_id = ?1 " +
            "ORDER BY r.route_id, s.shape_pt_sequence";

    // Scheduled time: MAX trip duration gives the full end-to-end trip (not a short partial trip).
    // Frequency: AVG headway from frequencies.txt when available; otherwise derived from the span
    // between the first and last trip departure on a weekday divided by (trips - 1).
    private static final String ROUTES_WITH_TIME_QUERY =
            "SELECT " +
            "    r.route_id, " +
            "    r.agency_id, " +
            "    r.route_short_name, " +
            "    r.route_long_name, " +
            "    MAX(s.shape_dist_traveled)           AS distance_km, " +
            "    COALESCE(sched.scheduled_minutes, 0) AS scheduled_time_minutes, " +
            "    COALESCE(freq.frequency_minutes, trips_freq.frequency_minutes, 0) AS frequency_minutes " +
            "FROM routes r " +
            "INNER JOIN trips t  ON t.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id  = t.shape_id " +
            "LEFT JOIN ( " +
            "    SELECT t2.route_id, " +
            "           MAX(trip_sched.sched_mins) AS scheduled_minutes " +
            "    FROM trips t2 " +
            "    INNER JOIN ( " +
            "        SELECT trip_id, " +
            "               ROUND((TIME_TO_SEC(MAX(arrival_time)) " +
            "                    - TIME_TO_SEC(MIN(departure_time))) / 60) AS sched_mins " +
            "        FROM stop_times " +
            "        GROUP BY trip_id " +
            "    ) trip_sched ON trip_sched.trip_id = t2.trip_id " +
            "    GROUP BY t2.route_id " +
            ") sched ON sched.route_id = r.route_id " +
            "LEFT JOIN ( " +
            "    SELECT t3.route_id, " +
            "           ROUND(AVG(f.headway_secs) / 60) AS frequency_minutes " +
            "    FROM trips t3 " +
            "    INNER JOIN frequencies f ON f.trip_id = t3.trip_id " +
            "    GROUP BY t3.route_id " +
            ") freq ON freq.route_id = r.route_id " +
            "LEFT JOIN ( " +
            "    SELECT t4.route_id, " +
            "           GREATEST(1, ROUND( " +
            "               (TIME_TO_SEC(MAX(fd.dep_time)) - TIME_TO_SEC(MIN(fd.dep_time))) " +
            "               / GREATEST(COUNT(DISTINCT t4.trip_id) - 1, 1) / 60 " +
            "           )) AS frequency_minutes " +
            "    FROM trips t4 " +
            "    INNER JOIN calendar c4 ON c4.service_id = t4.service_id AND c4.monday = 1 " +
            "    INNER JOIN ( " +
            "        SELECT trip_id, MIN(departure_time) AS dep_time " +
            "        FROM stop_times WHERE stop_sequence = 1 " +
            "        GROUP BY trip_id " +
            "    ) fd ON fd.trip_id = t4.trip_id " +
            "    GROUP BY t4.route_id " +
            "    HAVING COUNT(DISTINCT t4.trip_id) > 1 " +
            ") trips_freq ON trips_freq.route_id = r.route_id " +
            "GROUP BY r.route_id, r.agency_id, r.route_short_name, r.route_long_name, " +
            "         sched.scheduled_minutes, freq.frequency_minutes, trips_freq.frequency_minutes " +
            "HAVING MAX(s.shape_dist_traveled) > 0 " +
            "ORDER BY r.route_short_name";

    private static final String ROUTE_BY_ID_WITH_TIME_QUERY =
            "SELECT " +
            "    r.route_id, r.agency_id, r.route_short_name, r.route_long_name, " +
            "    MAX(s.shape_dist_traveled) AS distance_km, " +
            "    COALESCE(sched.scheduled_minutes, 0) AS scheduled_time_minutes, " +
            "    COALESCE(freq.frequency_minutes, trips_freq.frequency_minutes, 0) AS frequency_minutes " +
            "FROM routes r " +
            "INNER JOIN trips t ON t.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = t.shape_id " +
            "LEFT JOIN ( " +
            "    SELECT t2.route_id, MAX(trip_sched.sched_mins) AS scheduled_minutes " +
            "    FROM trips t2 " +
            "    INNER JOIN ( " +
            "        SELECT trip_id, " +
            "               ROUND((TIME_TO_SEC(MAX(arrival_time)) " +
            "                    - TIME_TO_SEC(MIN(departure_time))) / 60) AS sched_mins " +
            "        FROM stop_times GROUP BY trip_id " +
            "    ) trip_sched ON trip_sched.trip_id = t2.trip_id " +
            "    GROUP BY t2.route_id " +
            ") sched ON sched.route_id = r.route_id " +
            "LEFT JOIN ( " +
            "    SELECT t3.route_id, ROUND(AVG(f.headway_secs) / 60) AS frequency_minutes " +
            "    FROM trips t3 " +
            "    INNER JOIN frequencies f ON f.trip_id = t3.trip_id " +
            "    GROUP BY t3.route_id " +
            ") freq ON freq.route_id = r.route_id " +
            "LEFT JOIN ( " +
            "    SELECT t4.route_id, " +
            "           GREATEST(1, ROUND( " +
            "               (TIME_TO_SEC(MAX(fd.dep_time)) - TIME_TO_SEC(MIN(fd.dep_time))) " +
            "               / GREATEST(COUNT(DISTINCT t4.trip_id) - 1, 1) / 60 " +
            "           )) AS frequency_minutes " +
            "    FROM trips t4 " +
            "    INNER JOIN calendar c4 ON c4.service_id = t4.service_id AND c4.monday = 1 " +
            "    INNER JOIN ( " +
            "        SELECT trip_id, MIN(departure_time) AS dep_time " +
            "        FROM stop_times WHERE stop_sequence = 1 " +
            "        GROUP BY trip_id " +
            "    ) fd ON fd.trip_id = t4.trip_id " +
            "    GROUP BY t4.route_id " +
            "    HAVING COUNT(DISTINCT t4.trip_id) > 1 " +
            ") trips_freq ON trips_freq.route_id = r.route_id " +
            "WHERE r.route_id = ?1 " +
            "GROUP BY r.route_id, r.agency_id, r.route_short_name, r.route_long_name, " +
            "         sched.scheduled_minutes, freq.frequency_minutes, trips_freq.frequency_minutes " +
            "HAVING MAX(s.shape_dist_traveled) > 0";

    // shape_dist_traveled is optional in GTFS (e.g. RTP does not populate it).
    // When it is absent, compute the route length by summing ST_Distance_Sphere
    // between consecutive shape points of the first trip that has a shape.
    private static final String ROUTE_BY_ID_WITH_DISTANCE_QUERY =
            "SELECT r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type, " +
            "  CASE WHEN MAX(s.shape_dist_traveled) > 0 THEN MAX(s.shape_dist_traveled) " +
            "       ELSE ( " +
            "         SELECT SUM(ST_Distance_Sphere( " +
            "                  POINT(s1.shape_pt_lon, s1.shape_pt_lat), " +
            "                  POINT(s2.shape_pt_lon, s2.shape_pt_lat) " +
            "                )) / 1000 " +
            "         FROM shapes s1 " +
            "         JOIN shapes s2 ON s2.shape_id = s1.shape_id " +
            "                       AND s2.shape_pt_sequence = s1.shape_pt_sequence + 1 " +
            "         WHERE s1.shape_id = ( " +
            "           SELECT MIN(t2.shape_id) FROM trips t2 " +
            "           WHERE t2.route_id = r.route_id AND t2.shape_id IS NOT NULL " +
            "         ) " +
            "       ) " +
            "  END AS distance_km " +
            "FROM routes r " +
            "INNER JOIN trips t ON t.route_id = r.route_id " +
            "INNER JOIN shapes s ON s.shape_id = t.shape_id " +
            "WHERE r.route_id = ?1 " +
            "GROUP BY r.route_id, r.agency_id, r.route_short_name, r.route_long_name, r.route_type";

    private final EntityManager entityManager;

    @Inject
    public RouteRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

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

    @Override
    public Optional<RouteTimeComparison> findByIdWithTimeComparison(String routeId) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTE_BY_ID_WITH_TIME_QUERY)
                .setParameter(1, routeId)
                .getResultList();

        return results.stream().findFirst().map(row -> {
            RouteTimeComparison r = new RouteTimeComparison();
            r.setRouteId((String) row[0]);
            r.setAgencyId((String) row[1]);
            r.setRouteShortName((String) row[2]);
            r.setRouteLongName((String) row[3]);
            r.setDistanceKm(row[4] instanceof BigDecimal bd
                    ? bd.doubleValue()
                    : ((Number) row[4]).doubleValue());
            r.setScheduledTimeMinutes(((Number) row[5]).intValue());
            r.setFrequencyMinutes(((Number) row[6]).intValue());
            return r;
        });
    }

    @Override
    public List<RouteGeometry> findAllWithShapes() {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTES_WITH_SHAPES_QUERY)
                .getResultList();

        return buildRouteGeometries(results);
    }

    private List<RouteGeometry> buildRouteGeometries(List<Object[]> results) {
        Map<String, RouteGeometry> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            String routeId = (String) row[0];
            RouteGeometry rg = map.computeIfAbsent(routeId, k -> {
                RouteGeometry g = new RouteGeometry();
                g.setRouteId(routeId);
                g.setAgencyId((String) row[1]);
                g.setRouteShortName((String) row[2]);
                g.setRouteLongName((String) row[3]);
                g.setRouteType(((Number) row[4]).intValue());
                g.setRouteColor((String) row[5]);
                g.setDistanceKm(0.0);
                g.setCoordinates(new ArrayList<>());
                return g;
            });

            double lon = row[6] instanceof BigDecimal bd6 ? bd6.doubleValue() : ((Number) row[6]).doubleValue();
            double lat = row[7] instanceof BigDecimal bd7 ? bd7.doubleValue() : ((Number) row[7]).doubleValue();
            rg.getCoordinates().add(new double[]{lon, lat});

            double dist = row[8] instanceof BigDecimal bd8 ? bd8.doubleValue() : ((Number) row[8]).doubleValue();
            if (dist > rg.getDistanceKm()) {
                rg.setDistanceKm(dist);
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public List<RouteGeometry> findByAgencyWithShapes(String agencyId) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTES_WITH_SHAPES_BY_AGENCY_QUERY)
                .setParameter(1, agencyId)
                .getResultList();

        return buildRouteGeometries(results);
    }

    @Override
    public List<RouteTimeComparison> findAllWithTimeComparison() {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(ROUTES_WITH_TIME_QUERY)
                .getResultList();

        List<RouteTimeComparison> list = new ArrayList<>();
        for (Object[] row : results) {
            RouteTimeComparison r = new RouteTimeComparison();
            r.setRouteId((String) row[0]);
            r.setAgencyId((String) row[1]);
            r.setRouteShortName((String) row[2]);
            r.setRouteLongName((String) row[3]);
            r.setDistanceKm(row[4] instanceof BigDecimal bd
                    ? bd.doubleValue()
                    : ((Number) row[4]).doubleValue());
            r.setScheduledTimeMinutes(((Number) row[5]).intValue());
            r.setFrequencyMinutes(((Number) row[6]).intValue());
            list.add(r);
        }
        return list;
    }

    private Route mapRow(Object[] row) {
        String routeId = (String) row[0];
        String agencyId = (String) row[1];
        String shortName = (String) row[2];
        String longName = (String) row[3];
        Integer routeType = ((Number) row[4]).intValue();
        Double distanceKm = row[5] instanceof BigDecimal bd
                ? bd.doubleValue()
                : ((Number) row[5]).doubleValue();

        return RouteMapper.toDomain(routeId, agencyId, shortName, longName, routeType, distanceKm);
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM RouteEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Route> items) {
        int count = 0;
        for (Route item : items) {
            RouteEntity entity = RouteMapper.toEntity(item);
            AgencyEntity agency = entityManager.getReference(AgencyEntity.class, item.getAgencyId());
            entity.setAgency(agency);
            entityManager.persist(entity);
            if (++count % 200 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
