package org.acme.domain.models;

public class HourlyBusDemand {

    private int hour;
    private int busesRequired;

    public HourlyBusDemand() {
        // intentionally empty
    }

    public HourlyBusDemand(int hour, int busesRequired) {
        this.hour = hour;
        this.busesRequired = busesRequired;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getBusesRequired() { return busesRequired; }
    public void setBusesRequired(int busesRequired) { this.busesRequired = busesRequired; }
}
