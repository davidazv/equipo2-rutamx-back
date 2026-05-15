package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.models.Trip;
import org.acme.domain.repository.TripRepository;
import org.acme.infrastructure.entities.CalendarEntity;
import org.acme.infrastructure.entities.RouteEntity;
import org.acme.infrastructure.entities.TripEntity;
import org.acme.infrastructure.mapper.TripMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TripRepositoryImpl implements TripRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Trip> findAll() {
        return entityManager
                .createQuery("SELECT t FROM TripEntity t", TripEntity.class)
                .getResultList()
                .stream()
                .map(TripMapper::toDomain)
                .collect(Collectors.toList());
    }

    private static final String TRIPS_COUNT_BY_ROUTE_AND_DAY_QUERY =
            "SELECT r.route_id, r.route_short_name, r.route_long_name, a.agency_id, a.agency_color, " +
            "SUM(CASE WHEN c.monday    = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.tuesday   = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.wednesday = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.thursday  = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.friday    = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.saturday  = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.sunday    = 1 THEN 1 ELSE 0 END) " +
            "FROM routes r " +
            "JOIN trips t ON t.route_id = r.route_id " +
            "JOIN calendar c ON c.service_id = t.service_id " +
            "JOIN agency a ON a.agency_id = r.agency_id " +
            "GROUP BY r.route_id, r.route_short_name, r.route_long_name, a.agency_id, a.agency_color " +
            "ORDER BY (" +
            "  SUM(CASE WHEN c.monday    = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.tuesday   = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.wednesday = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.thursday  = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.friday    = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.saturday  = 1 THEN 1 ELSE 0 END) + " +
            "  SUM(CASE WHEN c.sunday    = 1 THEN 1 ELSE 0 END)" +
            ") DESC";

    @Override
    public List<RouteTripsPerDay> findCountGroupedByRouteAndDay() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(TRIPS_COUNT_BY_ROUTE_AND_DAY_QUERY)
                .getResultList();

        List<RouteTripsPerDay> result = new ArrayList<>();
        for (Object[] row : rows) {
            RouteTripsPerDay r = new RouteTripsPerDay();
            r.setRouteId((String) row[0]);
            r.setRouteShortName((String) row[1]);
            r.setRouteLongName((String) row[2]);
            r.setAgencyId((String) row[3]);
            r.setAgencyColor((String) row[4]);
            r.setMonday(((Number) row[5]).intValue());
            r.setTuesday(((Number) row[6]).intValue());
            r.setWednesday(((Number) row[7]).intValue());
            r.setThursday(((Number) row[8]).intValue());
            r.setFriday(((Number) row[9]).intValue());
            r.setSaturday(((Number) row[10]).intValue());
            r.setSunday(((Number) row[11]).intValue());
            result.add(r);
        }
        return result;
    }

    // Estimated daily trips = SUM of (period_duration / headway) across all frequency windows per route.
    // TIME_TO_SEC handles GTFS times > 24:00:00 correctly in MySQL.
    private static final String FREQUENCY_DAILY_TRIPS_QUERY =
            "SELECT t.route_id, " +
            "SUM(FLOOR((TIME_TO_SEC(f.end_time) - TIME_TO_SEC(f.start_time)) / f.headway_secs)) AS daily_trips " +
            "FROM trips t " +
            "INNER JOIN frequencies f ON f.trip_id = t.trip_id " +
            "GROUP BY t.route_id";

    @Override
    public Map<String, Double> findFrequencyBasedDailyTrips() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(FREQUENCY_DAILY_TRIPS_QUERY)
                .getResultList();
        Map<String, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            String routeId = (String) row[0];
            double daily = ((Number) row[1]).doubleValue();
            result.put(routeId, daily);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM TripEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Trip> items) {
        int count = 0;
        for (Trip item : items) {
            TripEntity entity = TripMapper.toEntity(item);
            RouteEntity route = entityManager.getReference(RouteEntity.class, item.getRouteId());
            entity.setRoute(route);
            CalendarEntity calendar = entityManager.getReference(CalendarEntity.class, item.getServiceId());
            entity.setCalendar(calendar);
            entityManager.persist(entity);
            if (++count % 200 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
