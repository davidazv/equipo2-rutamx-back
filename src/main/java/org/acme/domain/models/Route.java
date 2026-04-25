package org.acme.domain.models;

public class Route {

    private String routeId;
    private String agencyId;
    private String routeShortName;
    private String routeLongName;
    private Integer routeType;
    private Double distanceKm;

    public Route() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public Integer getRouteType() { return routeType; }
    public void setRouteType(Integer routeType) { this.routeType = routeType; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
