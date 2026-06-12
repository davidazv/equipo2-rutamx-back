package org.acme.domain.models;

public class OperationalSummary {

    private int totalRoutes;
    private double avgDailyPassengers;
    private String peakHour;

    public OperationalSummary() {
        // intentionally empty
    }

    public int getTotalRoutes() { return totalRoutes; }
    public void setTotalRoutes(int totalRoutes) { this.totalRoutes = totalRoutes; }

    public double getAvgDailyPassengers() { return avgDailyPassengers; }
    public void setAvgDailyPassengers(double avgDailyPassengers) { this.avgDailyPassengers = avgDailyPassengers; }

    public String getPeakHour() { return peakHour; }
    public void setPeakHour(String peakHour) { this.peakHour = peakHour; }
}
