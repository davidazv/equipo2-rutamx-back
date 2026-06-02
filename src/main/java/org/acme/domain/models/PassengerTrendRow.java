package org.acme.domain.models;

public class PassengerTrendRow {

    private int dow;
    private double avgPassengers;

    public PassengerTrendRow() {}

    public PassengerTrendRow(int dow, double avgPassengers) {
        this.dow = dow;
        this.avgPassengers = avgPassengers;
    }

    public int getDow() { return dow; }
    public void setDow(int dow) { this.dow = dow; }

    public double getAvgPassengers() { return avgPassengers; }
    public void setAvgPassengers(double avgPassengers) { this.avgPassengers = avgPassengers; }
}
