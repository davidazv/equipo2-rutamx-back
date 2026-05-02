package org.acme.domain.repository;

import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.models.Trip;
import java.util.List;
import java.util.Map;

public interface TripRepository {
    List<Trip> findAll();
    void deleteAll();
    int createAll(List<Trip> items);
    List<RouteTripsPerDay> findCountGroupedByRouteAndDay();
    /** Estimated daily trips per route derived from GTFS frequency headways. */
    Map<String, Double> findFrequencyBasedDailyTrips();
}
