package org.acme.domain.models;

public class StopTime {

    private String tripId;
    private String stopId;
    private int stopSequence;
    private String arrivalTime;
    private String departureTime;
    private Byte timepoint;

    public StopTime() {
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public Byte getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(Byte timepoint) {
        this.timepoint = timepoint;
    }
}
