package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetConstants;
import org.acme.domain.models.RouteTimeComparison;
import org.acme.domain.repository.RouteRepository;

import java.util.List;

/**
 * HU19 — Retrieves per-route GTFS travel time comparison for the COO dashboard.
 *
 * Scheduled time comes from the repository (derived from stop_times).
 * Estimated time and derived metrics are computed here using a fixed reference speed.
 */
@ApplicationScoped
public class GetRouteTravelTimesUseCase {

    private final RouteRepository routeRepository;

    @Inject
    public GetRouteTravelTimesUseCase(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<RouteTimeComparison> execute() {
        List<RouteTimeComparison> routes = routeRepository.findAllWithTimeComparison();

        for (RouteTimeComparison r : routes) {
            double distanceKm = r.getDistanceKm();
            int scheduledMin = r.getScheduledTimeMinutes();

            // Estimated time: distance / reference speed
            int estimatedMin = (int) Math.round(distanceKm / FleetConstants.REFERENCE_SPEED_KMH * 60.0);
            r.setEstimatedTimeMinutes(estimatedMin);

            // Average speed based on scheduled time (0 if no stop_times data)
            double avgSpeed = scheduledMin > 0
                    ? Math.round(distanceKm / (scheduledMin / 60.0) * 10.0) / 10.0
                    : 0.0;
            r.setAvgSpeedKmH(avgSpeed);

            // Variability: how much the estimate deviates from the schedule (0 if no data)
            double variability = scheduledMin > 0
                    ? Math.round(((estimatedMin - scheduledMin) / (double) scheduledMin * 100.0) * 10.0) / 10.0
                    : 0.0;
            r.setVariabilityPercent(variability);
        }

        return routes;
    }
}
