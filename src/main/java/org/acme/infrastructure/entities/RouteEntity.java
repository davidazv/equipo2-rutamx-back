package org.acme.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class RouteEntity {

    @Id
    @Column(name = "route_id", length = 50)
    private String routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

    @Column(name = "route_short_name", length = 20)
    private String routeShortName;

    @Column(name = "route_long_name", length = 255)
    private String routeLongName;

    @Column(name = "route_type", nullable = false)
    private Integer routeType;

    @Column(name = "route_color", length = 10)
    private String routeColor;

    @Column(name = "route_text_color", length = 10)
    private String routeTextColor;

    public RouteEntity() {
        // intentionally empty
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public AgencyEntity getAgency() { return agency; }
    public void setAgency(AgencyEntity agency) { this.agency = agency; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public String getRouteLongName() { return routeLongName; }
    public void setRouteLongName(String routeLongName) { this.routeLongName = routeLongName; }

    public Integer getRouteType() { return routeType; }
    public void setRouteType(Integer routeType) { this.routeType = routeType; }

    public String getRouteColor() { return routeColor; }
    public void setRouteColor(String routeColor) { this.routeColor = routeColor; }

    public String getRouteTextColor() { return routeTextColor; }
    public void setRouteTextColor(String routeTextColor) { this.routeTextColor = routeTextColor; }
}
