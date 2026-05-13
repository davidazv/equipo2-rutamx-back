package org.acme.domain.models;

import java.util.List;

public class CostBenefitReport {

    private String routeId;
    private double routeDistanceKm;
    private int numberOfBuses;
    private String electricModelName;
    private String dieselModelName;
    private double totalInvestmentMXN;
    private double paybackYears;
    private List<CostBenefitPoint> points;

    public CostBenefitReport() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public int getNumberOfBuses() { return numberOfBuses; }
    public void setNumberOfBuses(int numberOfBuses) { this.numberOfBuses = numberOfBuses; }

    public String getElectricModelName() { return electricModelName; }
    public void setElectricModelName(String electricModelName) { this.electricModelName = electricModelName; }

    public String getDieselModelName() { return dieselModelName; }
    public void setDieselModelName(String dieselModelName) { this.dieselModelName = dieselModelName; }

    public double getTotalInvestmentMXN() { return totalInvestmentMXN; }
    public void setTotalInvestmentMXN(double totalInvestmentMXN) { this.totalInvestmentMXN = totalInvestmentMXN; }

    public double getPaybackYears() { return paybackYears; }
    public void setPaybackYears(double paybackYears) { this.paybackYears = paybackYears; }

    public List<CostBenefitPoint> getPoints() { return points; }
    public void setPoints(List<CostBenefitPoint> points) { this.points = points; }

    public static class CostBenefitPoint {
        private int year;
        private double electricCumulativeMXN;
        private double dieselCumulativeMXN;
        private boolean breakEvenYear;

        public CostBenefitPoint() {}

        public CostBenefitPoint(int year, double electricCumulativeMXN, double dieselCumulativeMXN, boolean breakEvenYear) {
            this.year = year;
            this.electricCumulativeMXN = electricCumulativeMXN;
            this.dieselCumulativeMXN = dieselCumulativeMXN;
            this.breakEvenYear = breakEvenYear;
        }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public double getElectricCumulativeMXN() { return electricCumulativeMXN; }
        public void setElectricCumulativeMXN(double electricCumulativeMXN) { this.electricCumulativeMXN = electricCumulativeMXN; }

        public double getDieselCumulativeMXN() { return dieselCumulativeMXN; }
        public void setDieselCumulativeMXN(double dieselCumulativeMXN) { this.dieselCumulativeMXN = dieselCumulativeMXN; }

        public boolean isBreakEvenYear() { return breakEvenYear; }
        public void setBreakEvenYear(boolean breakEvenYear) { this.breakEvenYear = breakEvenYear; }
    }
}
