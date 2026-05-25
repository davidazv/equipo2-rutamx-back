package org.acme.domain.models;

public class HourlyOccupancy {

    private int hour;
    private double occupancyPct;

    public HourlyOccupancy() {}

    public HourlyOccupancy(int hour, double occupancyPct) {
        this.hour = hour;
        this.occupancyPct = occupancyPct;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public double getOccupancyPct() { return occupancyPct; }
    public void setOccupancyPct(double occupancyPct) { this.occupancyPct = occupancyPct; }
}
