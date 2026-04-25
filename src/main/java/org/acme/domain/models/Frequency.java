package org.acme.domain.models;

public class Frequency {

    private String tripId;
    private String startTime;
    private String endTime;
    private int headwaySecs;
    private Byte exactTimes;

    public Frequency() {
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getHeadwaySecs() {
        return headwaySecs;
    }

    public void setHeadwaySecs(int headwaySecs) {
        this.headwaySecs = headwaySecs;
    }

    public Byte getExactTimes() {
        return exactTimes;
    }

    public void setExactTimes(Byte exactTimes) {
        this.exactTimes = exactTimes;
    }
}
