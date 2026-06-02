package org.acme.domain.models;

public class CmoDashboardRouteRow {

    private String routeId;
    private String routeShortName;
    private String routeLongName;
    private String agencyId;
    private String routeColor;
    private double distanciaKm;
    private long totalTrips;
    private double avgDailyTrips;
    private int headwayMinutes;
    private double annualKm;
    private double co2DieselTonAnio;
    private double co2ElectricTonAnio;

    public CmoDashboardRouteRow() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getRouteColor() { return routeColor; }
    public void setRouteColor(String routeColor) { this.routeColor = routeColor; }

    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }

    public long getTotalTrips() { return totalTrips; }
    public void setTotalTrips(long totalTrips) { this.totalTrips = totalTrips; }

    public double getAvgDailyTrips() { return avgDailyTrips; }
    public void setAvgDailyTrips(double avgDailyTrips) { this.avgDailyTrips = avgDailyTrips; }

    public int getHeadwayMinutes() { return headwayMinutes; }
    public void setHeadwayMinutes(int headwayMinutes) { this.headwayMinutes = headwayMinutes; }

    public double getAnnualKm() { return annualKm; }
    public void setAnnualKm(double annualKm) { this.annualKm = annualKm; }

    public double getCo2DieselTonAnio() { return co2DieselTonAnio; }
    public void setCo2DieselTonAnio(double co2DieselTonAnio) { this.co2DieselTonAnio = co2DieselTonAnio; }

    public double getCo2ElectricTonAnio() { return co2ElectricTonAnio; }
    public void setCo2ElectricTonAnio(double co2ElectricTonAnio) { this.co2ElectricTonAnio = co2ElectricTonAnio; }
}
