package org.acme.domain.models;

public class TcoDataPoint {

    private int year;
    private double electricTCO;
    private double dieselTCO;

    public TcoDataPoint() {
        // intentionally empty
    }

    public TcoDataPoint(int year, double electricTCO, double dieselTCO) {
        this.year = year;
        this.electricTCO = electricTCO;
        this.dieselTCO = dieselTCO;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getElectricTCO() { return electricTCO; }
    public void setElectricTCO(double electricTCO) { this.electricTCO = electricTCO; }

    public double getDieselTCO() { return dieselTCO; }
    public void setDieselTCO(double dieselTCO) { this.dieselTCO = dieselTCO; }
}
