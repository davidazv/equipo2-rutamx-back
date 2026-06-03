package org.acme.domain.models;

public class OperationalCounters {

    private long totalRoutes;
    private long totalTrips;
    private long totalStops;
    private long totalShapes;
    private double avgFrequencyMin;

    public OperationalCounters() {}

    public long getTotalRoutes() { return totalRoutes; }
    public void setTotalRoutes(long totalRoutes) { this.totalRoutes = totalRoutes; }

    public long getTotalTrips() { return totalTrips; }
    public void setTotalTrips(long totalTrips) { this.totalTrips = totalTrips; }

    public long getTotalStops() { return totalStops; }
    public void setTotalStops(long totalStops) { this.totalStops = totalStops; }

    public long getTotalShapes() { return totalShapes; }
    public void setTotalShapes(long totalShapes) { this.totalShapes = totalShapes; }

    public double getAvgFrequencyMin() { return avgFrequencyMin; }
    public void setAvgFrequencyMin(double avgFrequencyMin) { this.avgFrequencyMin = avgFrequencyMin; }
}
