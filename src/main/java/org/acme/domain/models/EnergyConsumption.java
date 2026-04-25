package org.acme.domain.models;

public class EnergyConsumption {

    private String routeId;
    private double routeDistanceKm;
    private Long busModelId;
    private String busModelName;
    private int occupancyPercent;
    private double estimatedConsumptionKwh;
    private double batteryPercentAfter;
    private double remainingRangeKm;
    private boolean canCompleteRoute;

    public EnergyConsumption() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public Long getBusModelId() { return busModelId; }
    public void setBusModelId(Long busModelId) { this.busModelId = busModelId; }

    public String getBusModelName() { return busModelName; }
    public void setBusModelName(String busModelName) { this.busModelName = busModelName; }

    public int getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(int occupancyPercent) { this.occupancyPercent = occupancyPercent; }

    public double getEstimatedConsumptionKwh() { return estimatedConsumptionKwh; }
    public void setEstimatedConsumptionKwh(double estimatedConsumptionKwh) { this.estimatedConsumptionKwh = estimatedConsumptionKwh; }

    public double getBatteryPercentAfter() { return batteryPercentAfter; }
    public void setBatteryPercentAfter(double batteryPercentAfter) { this.batteryPercentAfter = batteryPercentAfter; }

    public double getRemainingRangeKm() { return remainingRangeKm; }
    public void setRemainingRangeKm(double remainingRangeKm) { this.remainingRangeKm = remainingRangeKm; }

    public boolean isCanCompleteRoute() { return canCompleteRoute; }
    public void setCanCompleteRoute(boolean canCompleteRoute) { this.canCompleteRoute = canCompleteRoute; }
}
