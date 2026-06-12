package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.models.TripsByDayResult;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.TripRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class GetTripsByDayUseCase {

    private static final Logger log = Logger.getLogger(GetTripsByDayUseCase.class.getName());

    // Average passengers per trip used when no real afluencia data is available
    private static final int AVG_PASSENGERS_PER_TRIP = 79;

    private final TripRepository tripRepository;
    private final AfluenciaMetrobusRepository afluenciaRepository;

    @Inject
    public GetTripsByDayUseCase(TripRepository tripRepository,
                                AfluenciaMetrobusRepository afluenciaRepository) {
        this.tripRepository = tripRepository;
        this.afluenciaRepository = afluenciaRepository;
    }

    public List<TripsByDayResult> execute() {
        List<RouteTripsPerDay> tripsPerDay = tripRepository.findCountGroupedByRouteAndDay();
        if (tripsPerDay.isEmpty()) {
            throw new NoGtfsDataException();
        }

        // Build afluencia map: routeShortName -> total afluencia across all DOWs
        List<AfluenciaResumen> afluenciaData = afluenciaRepository.findGroupedByLineaAndDow();
        Map<String, Double> totalAfluenciaByLinea = new HashMap<>();
        Map<String, Integer> dowCountByLinea = new HashMap<>();
        for (AfluenciaResumen a : afluenciaData) {
            totalAfluenciaByLinea.merge(a.getLinea(), a.getTotalAfluencia(), Double::sum);
            dowCountByLinea.merge(a.getLinea(), 1, Integer::sum);
        }

        // Frequency-based daily trip estimates (fallback when afluencia is unavailable)
        Map<String, Double> freqDailyTrips = tripRepository.findFrequencyBasedDailyTrips();

        List<TripsByDayResult> results = new ArrayList<>();
        for (RouteTripsPerDay t : tripsPerDay) {
            int total = t.getMonday() + t.getTuesday() + t.getWednesday()
                    + t.getThursday() + t.getFriday() + t.getSaturday() + t.getSunday();

            Double totalAfluencia = totalAfluenciaByLinea.get(t.getRouteShortName());
            Integer dowCount = dowCountByLinea.get(t.getRouteShortName());

            Double demanda = null;
            String calidad = "Baja";
            if (totalAfluencia != null && dowCount != null && dowCount > 0) {
                // Real afluencia data available
                demanda = Math.round((totalAfluencia / dowCount) * 100.0) / 100.0;
                calidad = "Alta";
            } else {
                // Estimate from GTFS frequency headways: daily_trips * avg_capacity
                Double dailyTrips = freqDailyTrips.get(t.getRouteId());
                if (dailyTrips != null && dailyTrips > 0) {
                    demanda = Math.round(dailyTrips * AVG_PASSENGERS_PER_TRIP * 100.0) / 100.0;
                    // calidad stays "Baja" (estimated, not real data)
                }
            }

            TripsByDayResult result = new TripsByDayResult();
            result.setRouteId(t.getRouteId());
            result.setRouteName(t.getRouteLongName());
            result.setAgencyColor(t.getAgencyColor());
            result.setMonday(t.getMonday());
            result.setTuesday(t.getTuesday());
            result.setWednesday(t.getWednesday());
            result.setThursday(t.getThursday());
            result.setFriday(t.getFriday());
            result.setSaturday(t.getSaturday());
            result.setSunday(t.getSunday());
            result.setTotalSemanal(total);
            result.setDemandaDiariaPromedio(demanda);
            result.setCalidadDatos(calidad);
            results.add(result);
        }

        log.log(java.util.logging.Level.INFO, "Trips-by-day calculado para {0} rutas", results.size());
        return results;
    }
}
