package org.acme.domain.models;

/**
 * Per-route comparison of estimated vs. scheduled GTFS travel times.
 * Used by HU19 to expose operational variability to the COO role.
 */
public class RouteTimeComparison {

    private String routeId;
    private String agencyId;
    private String routeShortName;
    private String routeLongName;
    private double distanceKm;

    /** Scheduled trip duration derived from GTFS stop_times (first departure → last arrival). */
    private int scheduledTimeMinutes;

    /** Estimated trip duration: distanceKm / REFERENCE_SPEED_KMH * 60. */
    private int estimatedTimeMinutes;

    /** Average speed based on the scheduled time: distanceKm / (scheduledTimeMinutes / 60). */
    private double avgSpeedKmH;

    /**
     * Variability between estimated and scheduled times as a percentage.
     * Positive → route takes longer than the baseline estimate (potential inefficiency).
     * Negative → route runs faster than the baseline estimate.
     */
    private double variabilityPercent;

    /** Minimum headway in minutes derived from GTFS frequencies.headway_secs. 0 if no data. */
    private int frequencyMinutes;

    public RouteTimeComparison() {
        // intentionally empty
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getScheduledTimeMinutes() { return scheduledTimeMinutes; }
    public void setScheduledTimeMinutes(int scheduledTimeMinutes) { this.scheduledTimeMinutes = scheduledTimeMinutes; }

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public double getAvgSpeedKmH() { return avgSpeedKmH; }
    public void setAvgSpeedKmH(double avgSpeedKmH) { this.avgSpeedKmH = avgSpeedKmH; }

    public double getVariabilityPercent() { return variabilityPercent; }
    public void setVariabilityPercent(double variabilityPercent) { this.variabilityPercent = variabilityPercent; }

    public int getFrequencyMinutes() { return frequencyMinutes; }
    public void setFrequencyMinutes(int frequencyMinutes) { this.frequencyMinutes = frequencyMinutes; }
}
