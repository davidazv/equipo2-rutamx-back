package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetConstants;
import org.acme.application.exception.DemandNotFoundException;
import org.acme.domain.models.BusCountRecommendation;
import org.acme.domain.models.DayType;
import org.acme.domain.repository.AfluenciaMetrobusRepository;

import java.math.BigDecimal;
import java.util.logging.Logger;

@ApplicationScoped
public class RecommendBusCountUseCase {

    private static final Logger log = Logger.getLogger(RecommendBusCountUseCase.class.getName());
    private final AfluenciaMetrobusRepository afluenciaRepository;

    @Inject
    public RecommendBusCountUseCase(AfluenciaMetrobusRepository afluenciaRepository) {
        this.afluenciaRepository = afluenciaRepository;
    }

    public BusCountRecommendation execute(String linea, String dayTypeStr, Integer occupancyPercent) {
        DayType dayType = DayType.fromString(dayTypeStr);
        int occ = (occupancyPercent == null) ? 80 : occupancyPercent;
        double targetOccupancy = occ / 100.0;

        BigDecimal avgDemand = afluenciaRepository.findAverageDailyDemand(linea, dayType);
        if (avgDemand == null) throw new DemandNotFoundException(linea);

        double avgDailyDemand = avgDemand.doubleValue();
        double peakHourDemand = avgDailyDemand * FleetConstants.PEAK_HOUR_FACTOR;
        int recommendedBuses = (int) Math.ceil(
                peakHourDemand / (FleetConstants.DEFAULT_BUS_CAPACITY * targetOccupancy));

        log.info("Bus count: linea=" + linea + " dayType=" + dayType
                + " avgDemand=" + String.format("%.0f", avgDailyDemand)
                + " buses=" + recommendedBuses);

        BusCountRecommendation result = new BusCountRecommendation();
        result.setLinea(linea);
        result.setDayType(dayType.name().toLowerCase());
        result.setAvgDailyDemand(avgDailyDemand);
        result.setPeakHourDemand(peakHourDemand);
        result.setRecommendedBuses(recommendedBuses);
        result.setTargetOccupancy(targetOccupancy);
        return result;
    }
}
