package org.acme.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "trips")
public class TripEntity {

    @Id
    @Column(name = "trip_id", length = 100)
    private String tripId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private CalendarEntity calendar;

    @Column(name = "shape_id", length = 50)
    private String shapeId;

    @Column(name = "trip_headsign", length = 150)
    private String tripHeadsign;

    @Column(name = "trip_short_name", length = 50)
    private String tripShortName;

    @Column(name = "direction_id")
    private Byte directionId;

    public TripEntity() {}

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public RouteEntity getRoute() { return route; }
    public void setRoute(RouteEntity route) { this.route = route; }

    public CalendarEntity getCalendar() { return calendar; }
    public void setCalendar(CalendarEntity calendar) { this.calendar = calendar; }

    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }

    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }

    public String getTripShortName() { return tripShortName; }
    public void setTripShortName(String tripShortName) { this.tripShortName = tripShortName; }

    public Byte getDirectionId() { return directionId; }
    public void setDirectionId(Byte directionId) { this.directionId = directionId; }
}
