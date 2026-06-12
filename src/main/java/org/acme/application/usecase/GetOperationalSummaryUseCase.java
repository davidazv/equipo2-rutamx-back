package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.Frequency;
import org.acme.domain.models.OperationalSummary;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class GetOperationalSummaryUseCase {

    private final RouteRepository routeRepository;
    private final AfluenciaMetrobusRepository afluenciaRepository;
    private final FrequencyRepository frequencyRepository;

    @Inject
    public GetOperationalSummaryUseCase(RouteRepository routeRepository,
                                        AfluenciaMetrobusRepository afluenciaRepository,
                                        FrequencyRepository frequencyRepository) {
        this.routeRepository = routeRepository;
        this.afluenciaRepository = afluenciaRepository;
        this.frequencyRepository = frequencyRepository;
    }

    public OperationalSummary execute() {
        int totalRoutes = routeRepository.findAllWithDistance().size();
        double avgDailyPassengers = afluenciaRepository.findAvgDailyPassengers();
        String peakHour = computePeakHour(frequencyRepository.findAll());

        OperationalSummary summary = new OperationalSummary();
        summary.setTotalRoutes(totalRoutes);
        summary.setAvgDailyPassengers(Math.round(avgDailyPassengers));
        summary.setPeakHour(peakHour);
        return summary;
    }

    // Find the hour with the most concurrent active trips (= highest bus activity = peak).
    private String computePeakHour(List<Frequency> frequencies) {
        if (frequencies.isEmpty()) return "08:00";

        Map<Integer, Integer> tripsPerHour = new TreeMap<>();
        for (Frequency f : frequencies) {
            int start = parseHour(f.getStartTime());
            int end = parseHour(f.getEndTime());
            for (int h = start; h <= end; h++) {
                tripsPerHour.merge(h, 1, Integer::sum);
            }
        }

        int peakH = tripsPerHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(8);

        return String.format("%02d:00", peakH);
    }

    // GTFS allows times > 23:00 (e.g. 24:00 = midnight, 25:30 = 01:30 next day).
    // Clamp to 23 so all service stays within a single day's 0-23 range.
    private int parseHour(String time) {
        if (time == null || time.length() < 2) return 0;
        try {
            return Math.min(Integer.parseInt(time.substring(0, 2)), 23);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
