package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetConstants;
import org.acme.application.exception.DemandNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.DayType;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.ModelRecommendation;
import org.acme.domain.models.Route;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class RecommendBusModelUseCase {

    private static final Logger log = Logger.getLogger(RecommendBusModelUseCase.class.getName());

    private final AfluenciaMetrobusRepository afluenciaRepository;
    private final FrequencyRepository frequencyRepository;
    private final RouteRepository routeRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public RecommendBusModelUseCase(AfluenciaMetrobusRepository afluenciaRepository,
                                     FrequencyRepository frequencyRepository,
                                     RouteRepository routeRepository,
                                     BusModelRepository busModelRepository) {
        this.afluenciaRepository = afluenciaRepository;
        this.frequencyRepository = frequencyRepository;
        this.routeRepository = routeRepository;
        this.busModelRepository = busModelRepository;
    }

    public ModelRecommendation execute(String linea, String dayTypeStr, Integer occupancyPercent) {
        DayType dayType = DayType.fromString(dayTypeStr);
        int occ = (occupancyPercent == null) ? 80 : occupancyPercent;
        double targetOccupancy = occ / 100.0;

        BigDecimal avgDemand = afluenciaRepository.findAverageDailyDemand(linea, dayType);
        if (avgDemand == null) throw new DemandNotFoundException(linea);

        // Extract route short name: "linea 1" -> "1"
        String routeShortName = linea.replaceAll("\\D+", "").trim();

        Double avgHeadway = frequencyRepository.findAverageHeadwayByRouteShortName(routeShortName);
        if (avgHeadway == null) {
            throw new IllegalStateException(
                    "No se encontraron datos de frecuencia para la ruta: " + routeShortName);
        }

        Route route = routeRepository.findByAgencyAndShortName("MB", routeShortName).orElse(null);
        double routeDistanceKm = (route != null) ? route.getDistanceKm() : 0.0;

        double peakHourDemand = avgDemand.doubleValue() * FleetConstants.PEAK_HOUR_FACTOR;
        double busesPerHour = 3600.0 / avgHeadway;
        int requiredCapacity = (int) Math.ceil(peakHourDemand / (busesPerHour * targetOccupancy));

        List<BusModel> electricModels = busModelRepository.findByFuelType(FuelType.ELECTRIC);

        List<BusModel> eligible = electricModels.stream()
                .filter(m -> m.getPassengerCapacity() >= requiredCapacity)
                .filter(m -> m.getAutonomyKm().doubleValue() >= routeDistanceKm * 2)
                .sorted(Comparator.comparing(BusModel::getUnitCostUsd))
                .toList();

        Long cheapestId = eligible.isEmpty() ? null : eligible.get(0).getId();

        List<ModelRecommendation.ModelCandidate> candidates = eligible.stream()
                .map(m -> {
                    ModelRecommendation.ModelCandidate c = new ModelRecommendation.ModelCandidate();
                    c.setId(m.getId());
                    c.setName(m.getName());
                    c.setManufacturer(m.getManufacturer());
                    c.setPassengerCapacity(m.getPassengerCapacity());
                    c.setAutonomyKm(m.getAutonomyKm().doubleValue());
                    c.setUnitCostUsd(m.getUnitCostUsd().doubleValue());
                    c.setRecommended(m.getId().equals(cheapestId));
                    return c;
                })
                .toList();

        log.info("Model recommendation: linea=" + linea + " requiredCap=" + requiredCapacity
                + " eligible=" + candidates.size());

        ModelRecommendation result = new ModelRecommendation();
        result.setLinea(linea);
        result.setRequiredCapacity(requiredCapacity);
        result.setModels(candidates);
        return result;
    }
}
