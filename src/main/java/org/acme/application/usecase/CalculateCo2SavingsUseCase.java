package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.EmissionConstants;
import org.acme.application.FleetConstants;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.Co2SavingsResult;
import org.acme.domain.models.DetallesDia;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.TripRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class CalculateCo2SavingsUseCase {

    private static final Logger log = Logger.getLogger(CalculateCo2SavingsUseCase.class.getName());

    // DAYOFWEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
    private static final Map<Integer, String> DOW_TO_SPANISH = Map.of(
            1, "domingo",
            2, "lunes",
            3, "martes",
            4, "miercoles",
            5, "jueves",
            6, "viernes",
            7, "sabado"
    );

    private final RouteRepository routeRepository;
    private final BusModelRepository busModelRepository;
    private final TripRepository tripRepository;
    private final AfluenciaMetrobusRepository afluenciaRepository;

    @Inject
    public CalculateCo2SavingsUseCase(RouteRepository routeRepository,
                                      BusModelRepository busModelRepository,
                                      TripRepository tripRepository,
                                      AfluenciaMetrobusRepository afluenciaRepository) {
        this.routeRepository = routeRepository;
        this.busModelRepository = busModelRepository;
        this.tripRepository = tripRepository;
        this.afluenciaRepository = afluenciaRepository;
    }

    public List<Co2SavingsResult> execute(Long busModelId) {
        BusModel busModel = busModelRepository.findById(busModelId)
                .orElseThrow(() -> new BusModelNotFoundException(busModelId));

        if (busModel.getFuelType() != FuelType.ELECTRIC) {
            throw new IllegalArgumentException("El modelo seleccionado debe ser eléctrico");
        }

        List<Route> routes = routeRepository.findAllWithDistance();
        if (routes.isEmpty()) {
            throw new NoGtfsDataException();
        }

        List<RouteTripsPerDay> tripsPerDay = tripRepository.findCountGroupedByRouteAndDay();
        Map<String, RouteTripsPerDay> tripsByRouteId = new HashMap<>();
        for (RouteTripsPerDay t : tripsPerDay) {
            tripsByRouteId.put(t.getRouteId(), t);
        }

        // Build afluencia map: normalizedLinea -> (dow -> avgAfluencia)
        List<AfluenciaResumen> afluenciaData = afluenciaRepository.findGroupedByLineaAndDow();
        Map<String, Map<Integer, Double>> afluenciaByLinea = new HashMap<>();
        for (AfluenciaResumen a : afluenciaData) {
            afluenciaByLinea
                    .computeIfAbsent(normalizeLinea(a.getLinea()), k -> new HashMap<>())
                    .put(a.getDayOfWeek(), a.getTotalAfluencia());
        }
        if (!afluenciaByLinea.isEmpty()) {
            log.log(java.util.logging.Level.INFO, "afluenciaByLinea keys sample: {0}", afluenciaByLinea.keySet().stream().limit(5).toList());
        } else {
            log.warning("afluencia_metrobus no devolvió datos para el último año — se usará fallback constante");
        }

        double kwhPerKm = busModel.getEnergyConsumptionKwhKm().doubleValue();
        List<BusModel> dieselModels = busModelRepository.findByFuelType(FuelType.DIESEL);
        double dieselLPerKm = dieselModels.stream()
                .filter(m -> m.getFuelConsumptionLKm() != null && m.getFuelConsumptionLKm().doubleValue() > 0)
                .mapToDouble(m -> m.getFuelConsumptionLKm().doubleValue())
                .average()
                .orElse(EmissionConstants.DIESEL_LITERS_PER_100KM / 100.0);
        int annualMultiplier = FleetConstants.DAILY_TRIPS * FleetConstants.OPERATING_DAYS;

        List<Co2SavingsResult> results = new ArrayList<>();
        for (Route route : routes) {
            double distKm = route.getDistanceKm();
            double annualKm = distKm * annualMultiplier;

            double dieselTon = round2(annualKm * dieselLPerKm * EmissionConstants.DIESEL_CO2_KG_PER_LITER / 1000.0);
            double electricTon = round2(annualKm * kwhPerKm * EmissionConstants.ELECTRIC_CO2_KG_PER_KWH / 1000.0);
            double ahorroTon = round2(dieselTon - electricTon);

            RouteTripsPerDay trips = tripsByRouteId.get(route.getRouteId());
            Map<String, DetallesDia> detallesPorDia = buildDetallesPorDia(
                    route.getRouteShortName(), trips, afluenciaByLinea);

            Co2SavingsResult item = new Co2SavingsResult();
            item.setRouteId(route.getRouteId());
            item.setRouteName(route.getRouteLongName());
            item.setAgencyId(route.getAgencyId());
            item.setAgencyColor(trips != null ? trips.getAgencyColor() : null);
            item.setDistanciaKm(round2(distKm));
            item.setEmisionesDieselTon(dieselTon);
            item.setEmisionesElectricoTon(electricTon);
            item.setAhorroTon(ahorroTon);
            item.setDetallesPorDia(detallesPorDia);
            results.add(item);
        }

        assignScoresAndPriorities(results);

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        log.log(java.util.logging.Level.INFO, "CO2 savings calculado para {0} rutas, busModelId={1}", new Object[]{results.size(), busModelId});
        return results;
    }

    private void assignScoresAndPriorities(List<Co2SavingsResult> results) {
        double maxRaw = 0.0;
        for (Co2SavingsResult r : results) {
            double raw = r.getAhorroTon() * r.getDistanciaKm();
            if (raw > maxRaw) maxRaw = raw;
        }

        for (Co2SavingsResult r : results) {
            double raw = r.getAhorroTon() * r.getDistanciaKm();
            r.setScore(maxRaw > 0 ? round2((raw / maxRaw) * 100.0) : 0.0);
        }

        // Percentile thresholds over the current dataset:
        // top 20% → Alta, 40–80% → Media, bottom 40% → Baja
        List<Double> sorted = results.stream()
                .map(Co2SavingsResult::getScore)
                .sorted()
                .toList();
        int n = sorted.size();
        double p80 = sorted.get((int) Math.floor(0.80 * n));
        double p40 = sorted.get((int) Math.floor(0.40 * n));

        for (Co2SavingsResult r : results) {
            r.setPrioridad(r.getScore() >= p80 ? "Alta" : r.getScore() >= p40 ? "Media" : "Baja");
        }
    }

    // Average passengers per trip used when real afluencia data is unavailable
    private static final int AVG_PASSENGERS_PER_TRIP = 79;

    private Map<String, DetallesDia> buildDetallesPorDia(String routeShortName,
                                                          RouteTripsPerDay trips,
                                                          Map<String, Map<Integer, Double>> afluenciaByLinea) {
        if (trips == null) return null;

        int[] tripCounts = new int[]{
                trips.getSunday(), trips.getMonday(), trips.getTuesday(),
                trips.getWednesday(), trips.getThursday(), trips.getFriday(), trips.getSaturday()};

        Map<Integer, Double> pasajerosByDow = afluenciaByLinea.get(normalizeLinea(routeShortName));

        Map<String, DetallesDia> detalles = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : DOW_TO_SPANISH.entrySet()) {
            int dow = entry.getKey();
            String nombre = entry.getValue();
            int viajes = tripCounts[dow - 1];
            if (viajes == 0) continue;
            double pasajeros;
            if (pasajerosByDow != null) {
                Double afluencia = pasajerosByDow.get(dow);
                pasajeros = afluencia != null ? afluencia : (double) viajes * AVG_PASSENGERS_PER_TRIP;
            } else {
                pasajeros = (double) viajes * AVG_PASSENGERS_PER_TRIP;
            }
            detalles.put(nombre, new DetallesDia(viajes, round2(pasajeros)));
        }
        return detalles.isEmpty() ? null : detalles;
    }

    // Normalizes linea/route identifiers so "L1", "Línea 1", "linea 1" and "1" all match.
    static String normalizeLinea(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        s = s.replaceAll("^l[íi]nea\\s+", ""); // "línea 1" → "1"
        s = s.replaceAll("^l(?=\\d)", "");       // "L1"      → "1"
        return s;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
