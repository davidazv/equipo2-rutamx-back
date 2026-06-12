package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.exception.NoDemandDataException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.BusModelRank;
import org.acme.domain.models.BusModelRecommendation;
import org.acme.domain.models.BusModelRecommendation.DayRecommendation;
import org.acme.domain.models.BusModelRecommendation.DemandSummary;
import org.acme.domain.models.BusModelRecommendation.RecommendationsByDay;
import org.acme.domain.models.RouteTimeComparison;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HU12 — Recommends bus models for a route based on historical demand.
 *
 * Algorithm:
 *  1. Fetch route distance and frequency from GTFS.
 *  2. Map route_short_name → "linea N" to look up afluencia demand.
 *  3. For each day type (weekday / saturday / sunday):
 *     peakHourDemand  = round(avgDailyDemand × PEAK_HOUR_FACTOR)
 *     busesPerHour    = round(60 / frequencyMinutes)  [default DEFAULT_BUSES_PER_HOUR]
 *     requiredCapacity = ceil(peakHourDemand / (busesPerHour × targetOccupancy))
 *  4. Rank all bus models: eligible (meets capacity AND autonomy) first, sorted by
 *     unit cost ASC; ineligible after, sorted by capacity DESC.
 */
@ApplicationScoped
public class RecommendBusModelUseCase {

    private static final Logger log = Logger.getLogger(RecommendBusModelUseCase.class.getName());

    /** 12 % of daily demand is considered the peak-hour load. */
    private static final double PEAK_HOUR_FACTOR = 0.12;

    /** Fallback buses per hour when no GTFS frequency data is available. */
    private static final int DEFAULT_BUSES_PER_HOUR = 10;

    private final RouteRepository routeRepository;
    private final AfluenciaMetrobusRepository afluenciaRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public RecommendBusModelUseCase(RouteRepository routeRepository,
                                    AfluenciaMetrobusRepository afluenciaRepository,
                                    BusModelRepository busModelRepository) {
        this.routeRepository = routeRepository;
        this.afluenciaRepository = afluenciaRepository;
        this.busModelRepository = busModelRepository;
    }

    public BusModelRecommendation execute(String routeId, double targetOccupancy) {
        log.log(java.util.logging.Level.INFO, "Generating bus model recommendation: targetOccupancy={0}", targetOccupancy);

        // 1 — Fetch route with distance and frequency
        RouteTimeComparison route = routeRepository.findByIdWithTimeComparison(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Ruta no encontrada: " + routeId));

        // 2 — Build the afluencia lookup key: "linea " + shortName (e.g. "linea 1")
        String lineaKey = "linea " + route.getRouteShortName().toLowerCase();
        Map<String, Double> demandMap = afluenciaRepository.findAvgDemandByLinea(lineaKey);

        if (demandMap.isEmpty()) {
            throw new NoDemandDataException(
                    "No hay datos de afluencia para la ruta: " + routeId);
        }

        // 3 — Build demand summary
        DemandSummary demand = new DemandSummary();
        demand.setAvgWeekday(Math.round(demandMap.getOrDefault("weekday", 0.0)));
        demand.setAvgSaturday(Math.round(demandMap.getOrDefault("saturday", 0.0)));
        demand.setAvgSunday(Math.round(demandMap.getOrDefault("sunday", 0.0)));

        // 4 — Fetch all bus models once
        List<BusModel> models = busModelRepository.findAll();

        double distanceKm = route.getDistanceKm();
        int frequencyMinutes = route.getFrequencyMinutes();

        // 5 — Build ranked recommendations for each day type
        RecommendationsByDay recs = new RecommendationsByDay();
        recs.setWeekday(buildDayRecommendation(demand.getAvgWeekday(), frequencyMinutes, distanceKm, targetOccupancy, models));
        recs.setSaturday(buildDayRecommendation(demand.getAvgSaturday(), frequencyMinutes, distanceKm, targetOccupancy, models));
        recs.setSunday(buildDayRecommendation(demand.getAvgSunday(), frequencyMinutes, distanceKm, targetOccupancy, models));

        BusModelRecommendation result = new BusModelRecommendation();
        result.setRouteId(route.getRouteId());
        result.setRouteShortName(route.getRouteShortName());
        result.setRouteLongName(route.getRouteLongName());
        result.setDistanceKm(distanceKm);
        result.setFrequencyMinutes(frequencyMinutes);
        result.setDemand(demand);
        result.setRecommendations(recs);

        return result;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private DayRecommendation buildDayRecommendation(long avgDailyDemand,
                                                     int frequencyMinutes,
                                                     double distanceKm,
                                                     double targetOccupancy,
                                                     List<BusModel> models) {
        long peakHourDemand = Math.round(avgDailyDemand * PEAK_HOUR_FACTOR);
        int busesPerHour = frequencyMinutes > 0
                ? (int) Math.round(60.0 / frequencyMinutes)
                : DEFAULT_BUSES_PER_HOUR;
        int requiredCapacity = (int) Math.ceil(peakHourDemand / (busesPerHour * targetOccupancy));
        double minAutonomyKm = distanceKm * 2.0;

        List<BusModel> sorted = new ArrayList<>(models);
        sorted.sort(Comparator
                .comparing((BusModel m) -> !isEligible(m, requiredCapacity, minAutonomyKm))
                .thenComparing(m -> isEligible(m, requiredCapacity, minAutonomyKm)
                        ? m.getUnitCostUsd()
                        : null,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(m -> isEligible(m, requiredCapacity, minAutonomyKm)
                        ? null
                        : -m.getPassengerCapacity(),
                        Comparator.nullsFirst(Comparator.naturalOrder())));

        Long recommendedId = sorted.stream()
                .filter(m -> isEligible(m, requiredCapacity, minAutonomyKm))
                .findFirst()
                .map(BusModel::getId)
                .orElse(null);

        List<BusModelRank> ranks = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            BusModel m = sorted.get(i);
            boolean meetsCapacity = m.getPassengerCapacity() != null
                    && m.getPassengerCapacity() >= requiredCapacity;
            boolean meetsAutonomy = m.getAutonomyKm() != null
                    && m.getAutonomyKm().doubleValue() >= minAutonomyKm;

            BusModelRank rank = new BusModelRank();
            rank.setRank(i + 1);
            rank.setModel(m);
            rank.setMeetsCapacity(meetsCapacity);
            rank.setMeetsAutonomy(meetsAutonomy);
            rank.setRecommended(recommendedId != null && recommendedId.equals(m.getId()));
            rank.setRequiredCapacity(requiredCapacity);
            rank.setJustification(buildJustification(m, meetsCapacity, meetsAutonomy,
                    requiredCapacity, minAutonomyKm));
            ranks.add(rank);
        }

        DayRecommendation day = new DayRecommendation();
        day.setPeakHourDemand(peakHourDemand);
        day.setRequiredCapacity(requiredCapacity);
        day.setModels(ranks);
        return day;
    }

    private boolean isEligible(BusModel m, int requiredCapacity, double minAutonomyKm) {
        return m.getPassengerCapacity() != null && m.getPassengerCapacity() >= requiredCapacity
                && m.getAutonomyKm() != null && m.getAutonomyKm().doubleValue() >= minAutonomyKm;
    }

    private String buildJustification(BusModel m, boolean meetsCapacity, boolean meetsAutonomy,
                                      int requiredCapacity, double minAutonomyKm) {
        int capacity = m.getPassengerCapacity() != null ? m.getPassengerCapacity() : 0;
        int autonomy = m.getAutonomyKm() != null ? m.getAutonomyKm().intValue() : 0;
        int minAutonomy = (int) Math.ceil(minAutonomyKm);

        if (meetsCapacity && meetsAutonomy) {
            return String.format("Cumple capacidad (%d ≥ %d pas.) y autonomía (%d km ≥ %d km)",
                    capacity, requiredCapacity, autonomy, minAutonomy);
        }
        if (!meetsCapacity && !meetsAutonomy) {
            return String.format("Capacidad insuficiente (%d < %d pas.) y autonomía insuficiente (%d km < %d km)",
                    capacity, requiredCapacity, autonomy, minAutonomy);
        }
        if (!meetsCapacity) {
            return String.format("Capacidad insuficiente (%d < %d pas.)",
                    capacity, requiredCapacity);
        }
        return String.format("Autonomía insuficiente (%d km < %d km)",
                autonomy, minAutonomy);
    }
}
