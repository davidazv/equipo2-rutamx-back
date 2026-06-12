package org.acme.domain.models;

public class Trip {

    private String tripId;
    private String routeId;
    private String serviceId;
    private String shapeId;
    private String tripHeadsign;
    private String tripShortName;
    private Byte directionId;

    public Trip() {
        // intentionally empty
    }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }

    public String getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }

    public String getTripShortName() { return tripShortName; }
    public void setTripShortName(String tripShortName) { this.tripShortName = tripShortName; }

    public Byte getDirectionId() { return directionId; }
    public void setDirectionId(Byte directionId) { this.directionId = directionId; }
}
