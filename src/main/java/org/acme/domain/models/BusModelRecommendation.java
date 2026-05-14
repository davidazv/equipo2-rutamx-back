package org.acme.domain.models;

import java.util.List;

/**
 * Full bus model recommendation result for a specific route (HU12).
 *
 * Contains demand averages by day type and a ranked list of bus models
 * for weekday, saturday, and sunday scenarios.
 */
public class BusModelRecommendation {

    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private double distanceKm;
    private int frequencyMinutes;
    private DemandSummary demand;
    private RecommendationsByDay recommendations;

    public BusModelRecommendation() {}

    // ── Nested types ────────────────────────────────────────────────────────

    public static class DemandSummary {
        private long avgWeekday;
        private long avgSaturday;
        private long avgSunday;

        public DemandSummary() {}

        public long getAvgWeekday() { return avgWeekday; }
        public void setAvgWeekday(long avgWeekday) { this.avgWeekday = avgWeekday; }

        public long getAvgSaturday() { return avgSaturday; }
        public void setAvgSaturday(long avgSaturday) { this.avgSaturday = avgSaturday; }

        public long getAvgSunday() { return avgSunday; }
        public void setAvgSunday(long avgSunday) { this.avgSunday = avgSunday; }
    }

    public static class DayRecommendation {
        private long peakHourDemand;
        private int requiredCapacity;
        private List<BusModelRank> models;

        public DayRecommendation() {}

        public long getPeakHourDemand() { return peakHourDemand; }
        public void setPeakHourDemand(long peakHourDemand) { this.peakHourDemand = peakHourDemand; }

        public int getRequiredCapacity() { return requiredCapacity; }
        public void setRequiredCapacity(int requiredCapacity) { this.requiredCapacity = requiredCapacity; }

        public List<BusModelRank> getModels() { return models; }
        public void setModels(List<BusModelRank> models) { this.models = models; }
    }

    public static class RecommendationsByDay {
        private DayRecommendation weekday;
        private DayRecommendation saturday;
        private DayRecommendation sunday;

        public RecommendationsByDay() {}

        public DayRecommendation getWeekday() { return weekday; }
        public void setWeekday(DayRecommendation weekday) { this.weekday = weekday; }

        public DayRecommendation getSaturday() { return saturday; }
        public void setSaturday(DayRecommendation saturday) { this.saturday = saturday; }

        public DayRecommendation getSunday() { return sunday; }
        public void setSunday(DayRecommendation sunday) { this.sunday = sunday; }
    }

    // ── Main class accessors ─────────────────────────────────────────────────

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getFrequencyMinutes() { return frequencyMinutes; }
    public void setFrequencyMinutes(int frequencyMinutes) { this.frequencyMinutes = frequencyMinutes; }

    public DemandSummary getDemand() { return demand; }
    public void setDemand(DemandSummary demand) { this.demand = demand; }

    public RecommendationsByDay getRecommendations() { return recommendations; }
    public void setRecommendations(RecommendationsByDay recommendations) { this.recommendations = recommendations; }
}
