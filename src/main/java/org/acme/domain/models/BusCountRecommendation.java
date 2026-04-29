package org.acme.domain.models;

public class BusCountRecommendation {

    private String linea;
    private String dayType;
    private double avgDailyDemand;
    private double peakHourDemand;
    private int recommendedBuses;
    private double targetOccupancy;

    public BusCountRecommendation() {}

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public String getDayType() { return dayType; }
    public void setDayType(String dayType) { this.dayType = dayType; }

    public double getAvgDailyDemand() { return avgDailyDemand; }
    public void setAvgDailyDemand(double avgDailyDemand) { this.avgDailyDemand = avgDailyDemand; }

    public double getPeakHourDemand() { return peakHourDemand; }
    public void setPeakHourDemand(double peakHourDemand) { this.peakHourDemand = peakHourDemand; }

    public int getRecommendedBuses() { return recommendedBuses; }
    public void setRecommendedBuses(int recommendedBuses) { this.recommendedBuses = recommendedBuses; }

    public double getTargetOccupancy() { return targetOccupancy; }
    public void setTargetOccupancy(double targetOccupancy) { this.targetOccupancy = targetOccupancy; }
}
