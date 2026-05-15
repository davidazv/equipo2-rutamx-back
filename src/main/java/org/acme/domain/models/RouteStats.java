package org.acme.domain.models;

public class RouteStats {

    private String routeId;
    private String routeName;
    private String agencyId;
    private String agencyColor;
    private double distanciaKm;
    private double avgDailyPassengers;
    private double co2DieselTonAnio;
    private double co2ElectricoTonAnio;
    private double co2AhorradoTonAnio;
    private double avgDailyTrips;
    private int headwayMinutes;

    public RouteStats() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyColor() { return agencyColor; }
    public void setAgencyColor(String agencyColor) { this.agencyColor = agencyColor; }

    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }

    public double getAvgDailyPassengers() { return avgDailyPassengers; }
    public void setAvgDailyPassengers(double avgDailyPassengers) { this.avgDailyPassengers = avgDailyPassengers; }

    public double getCo2DieselTonAnio() { return co2DieselTonAnio; }
    public void setCo2DieselTonAnio(double co2DieselTonAnio) { this.co2DieselTonAnio = co2DieselTonAnio; }

    public double getCo2ElectricoTonAnio() { return co2ElectricoTonAnio; }
    public void setCo2ElectricoTonAnio(double co2ElectricoTonAnio) { this.co2ElectricoTonAnio = co2ElectricoTonAnio; }

    public double getCo2AhorradoTonAnio() { return co2AhorradoTonAnio; }
    public void setCo2AhorradoTonAnio(double co2AhorradoTonAnio) { this.co2AhorradoTonAnio = co2AhorradoTonAnio; }

    public double getAvgDailyTrips() { return avgDailyTrips; }
    public void setAvgDailyTrips(double avgDailyTrips) { this.avgDailyTrips = avgDailyTrips; }

    public int getHeadwayMinutes() { return headwayMinutes; }
    public void setHeadwayMinutes(int headwayMinutes) { this.headwayMinutes = headwayMinutes; }
}
