package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.EmissionConstants;
import org.acme.application.FleetConstants;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.models.RouteStats;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.TripRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetCmoRouteStatsUseCase {

    private static final int AVG_PASSENGERS_PER_TRIP = 79;

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final FrequencyRepository frequencyRepository;
    private final AfluenciaMetrobusRepository afluenciaRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public GetCmoRouteStatsUseCase(RouteRepository routeRepository,
                                   TripRepository tripRepository,
                                   FrequencyRepository frequencyRepository,
                                   AfluenciaMetrobusRepository afluenciaRepository,
                                   BusModelRepository busModelRepository) {
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.frequencyRepository = frequencyRepository;
        this.afluenciaRepository = afluenciaRepository;
        this.busModelRepository = busModelRepository;
    }

    public List<RouteStats> execute() {
        List<Route> routes = routeRepository.findAllWithDistance();

        Map<String, RouteTripsPerDay> tripsByRouteId = new HashMap<>();
        for (RouteTripsPerDay t : tripRepository.findCountGroupedByRouteAndDay()) {
            tripsByRouteId.put(t.getRouteId(), t);
        }

        // linea (normalized) → dow → avgAfluencia
        Map<String, Map<Integer, Double>> afluenciaByLinea = new HashMap<>();
        for (AfluenciaResumen a : afluenciaRepository.findGroupedByLineaAndDow()) {
            afluenciaByLinea
                    .computeIfAbsent(normalizeLinea(a.getLinea()), k -> new HashMap<>())
                    .put(a.getDayOfWeek(), a.getTotalAfluencia());
        }

        Map<String, Integer> headwayByRoute = frequencyRepository.findAvgHeadwaySecsByRoute();

        double kwhPerKm = resolveElectricKwhPerKm();
        double dieselLPerKm = resolveDieselLPerKm();
        int annualMultiplier = FleetConstants.DAILY_TRIPS * FleetConstants.OPERATING_DAYS;

        List<RouteStats> results = new ArrayList<>();
        for (Route route : routes) {
            RouteTripsPerDay trips = tripsByRouteId.get(route.getRouteId());

            double avgDailyTrips = computeAvgDailyTrips(trips);
            double avgDailyPassengers = computeAvgDailyPassengers(
                    route.getRouteShortName(), trips, afluenciaByLinea, avgDailyTrips);

            double distKm = route.getDistanceKm();
            double annualKm = distKm * annualMultiplier;
            double dieselTon = round2(annualKm * dieselLPerKm * EmissionConstants.DIESEL_CO2_KG_PER_LITER / 1000.0);
            double electricTon = round2(annualKm * kwhPerKm * EmissionConstants.ELECTRIC_CO2_KG_PER_KWH / 1000.0);

            int headwaySecs = headwayByRoute.getOrDefault(route.getRouteId(), 0);

            RouteStats stats = new RouteStats();
            stats.setRouteId(route.getRouteId());
            stats.setRouteName(route.getRouteShortName() != null
                    ? route.getRouteShortName() : route.getRouteLongName());
            stats.setAgencyId(route.getAgencyId());
            stats.setAgencyColor(trips != null ? trips.getAgencyColor() : null);
            stats.setDistanciaKm(round2(distKm));
            stats.setAvgDailyPassengers(round2(avgDailyPassengers));
            stats.setCo2DieselTonAnio(dieselTon);
            stats.setCo2ElectricoTonAnio(electricTon);
            stats.setCo2AhorradoTonAnio(round2(dieselTon - electricTon));
            stats.setAvgDailyTrips(round2(avgDailyTrips));
            stats.setHeadwayMinutes(headwaySecs > 0 ? headwaySecs / 60 : 0);

            results.add(stats);
        }

        return results;
    }

    private double computeAvgDailyTrips(RouteTripsPerDay trips) {
        if (trips == null) return 0.0;
        int[] counts = {
            trips.getMonday(), trips.getTuesday(), trips.getWednesday(),
            trips.getThursday(), trips.getFriday(), trips.getSaturday(), trips.getSunday()
        };
        int sum = 0, active = 0;
        for (int c : counts) {
            if (c > 0) { sum += c; active++; }
        }
        return active > 0 ? (double) sum / active : 0.0;
    }

    private double computeAvgDailyPassengers(String routeShortName,
                                              RouteTripsPerDay trips,
                                              Map<String, Map<Integer, Double>> afluenciaByLinea,
                                              double avgDailyTrips) {
        Map<Integer, Double> afluenciaDow = afluenciaByLinea.get(normalizeLinea(routeShortName));
        if (afluenciaDow != null && !afluenciaDow.isEmpty()) {
            double sum = 0;
            for (double v : afluenciaDow.values()) sum += v;
            return sum / afluenciaDow.size();
        }
        return avgDailyTrips * AVG_PASSENGERS_PER_TRIP;
    }

    private double resolveElectricKwhPerKm() {
        return busModelRepository.findByFuelType(FuelType.ELECTRIC).stream()
                .filter(m -> m.getEnergyConsumptionKwhKm() != null)
                .mapToDouble(m -> m.getEnergyConsumptionKwhKm().doubleValue())
                .average()
                .orElse(1.2);
    }

    private double resolveDieselLPerKm() {
        return busModelRepository.findByFuelType(FuelType.DIESEL).stream()
                .filter(m -> m.getFuelConsumptionLKm() != null && m.getFuelConsumptionLKm().doubleValue() > 0)
                .mapToDouble(m -> m.getFuelConsumptionLKm().doubleValue())
                .average()
                .orElse(EmissionConstants.DIESEL_LITERS_PER_100KM / 100.0);
    }

    static String normalizeLinea(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        s = s.replaceAll("^l[íi]nea\\s+", "");
        s = s.replaceAll("^l(?=\\d)", "");
        return s;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
