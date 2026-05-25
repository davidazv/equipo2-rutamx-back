package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.Frequency;
import org.acme.domain.models.HourlyBusDemand;
import org.acme.domain.models.HourlyFrequencyStats;
import org.acme.domain.models.HourlyOccupancy;
import org.acme.domain.repository.FrequencyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class GetHourlyFrequencyStatsUseCase {

    private static final double MIN_OCCUPANCY_PCT = 20.0;
    private static final double MAX_OCCUPANCY_PCT = 95.0;

    private final FrequencyRepository frequencyRepository;

    @Inject
    public GetHourlyFrequencyStatsUseCase(FrequencyRepository frequencyRepository) {
        this.frequencyRepository = frequencyRepository;
    }

    public HourlyFrequencyStats execute() {
        List<Frequency> all = frequencyRepository.findAll();

        // For each hour accumulate buses required = sum(3600 / headway_secs) for active trips
        Map<Integer, Integer> busesPerHour = new TreeMap<>();
        for (Frequency f : all) {
            int start = parseHour(f.getStartTime());
            int end = parseHour(f.getEndTime());
            int busesThisTrip = (int) Math.round(3600.0 / f.getHeadwaySecs());
            for (int h = start; h <= end; h++) {
                busesPerHour.merge(h, busesThisTrip, Integer::sum);
            }
        }

        List<HourlyBusDemand> busDemand = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : busesPerHour.entrySet()) {
            busDemand.add(new HourlyBusDemand(e.getKey(), e.getValue()));
        }

        // Normalize buses to occupancy range [MIN_OCCUPANCY_PCT, MAX_OCCUPANCY_PCT]
        int maxBuses = busDemand.stream().mapToInt(HourlyBusDemand::getBusesRequired).max().orElse(1);
        int minBuses = busDemand.stream().mapToInt(HourlyBusDemand::getBusesRequired).min().orElse(0);
        double range = maxBuses - minBuses;

        List<HourlyOccupancy> occupancy = new ArrayList<>();
        for (HourlyBusDemand bd : busDemand) {
            double pct = range == 0
                    ? (MIN_OCCUPANCY_PCT + MAX_OCCUPANCY_PCT) / 2
                    : MIN_OCCUPANCY_PCT + (MAX_OCCUPANCY_PCT - MIN_OCCUPANCY_PCT) * (bd.getBusesRequired() - minBuses) / range;
            occupancy.add(new HourlyOccupancy(bd.getHour(), Math.round(pct * 10.0) / 10.0));
        }

        return new HourlyFrequencyStats(occupancy, busDemand);
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
