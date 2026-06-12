package org.acme.domain.models;

public class HourlyTripRow {

    private int hour;
    private long tripCount;

    public HourlyTripRow() {
        // intentionally empty
    }

    public HourlyTripRow(int hour, long tripCount) {
        this.hour = hour;
        this.tripCount = tripCount;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public long getTripCount() { return tripCount; }
    public void setTripCount(long tripCount) { this.tripCount = tripCount; }
}
