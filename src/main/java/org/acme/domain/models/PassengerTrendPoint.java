package org.acme.domain.models;

public class PassengerTrendPoint {

    private String day;
    private double avgPassengers;

    public PassengerTrendPoint() {}

    public PassengerTrendPoint(String day, double avgPassengers) {
        this.day = day;
        this.avgPassengers = avgPassengers;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public double getAvgPassengers() { return avgPassengers; }
    public void setAvgPassengers(double avgPassengers) { this.avgPassengers = avgPassengers; }
}
