package org.acme.domain.models;

public class ComparativeReport {

    private String routeId;
    private double routeDistanceKm;
    private int numberOfBuses;

    private String electricModelName;
    private double electricCostPerYear;
    private double electricMaintenanceCostPerYear;
    private double electricTotalCostPerYear;
    private double electricCo2TonsPerYear;

    private String dieselModelName;
    private double dieselCostPerYear;
    private double dieselMaintenanceCostPerYear;
    private double dieselTotalCostPerYear;
    private double dieselCo2TonsPerYear;

    private double annualSavingsMXN;
    private double co2AvoidedTonsPerYear;
    private double savingsPercent;

    public ComparativeReport() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public int getNumberOfBuses() { return numberOfBuses; }
    public void setNumberOfBuses(int numberOfBuses) { this.numberOfBuses = numberOfBuses; }

    public String getElectricModelName() { return electricModelName; }
    public void setElectricModelName(String electricModelName) { this.electricModelName = electricModelName; }

    public double getElectricCostPerYear() { return electricCostPerYear; }
    public void setElectricCostPerYear(double electricCostPerYear) { this.electricCostPerYear = electricCostPerYear; }

    public double getElectricMaintenanceCostPerYear() { return electricMaintenanceCostPerYear; }
    public void setElectricMaintenanceCostPerYear(double electricMaintenanceCostPerYear) { this.electricMaintenanceCostPerYear = electricMaintenanceCostPerYear; }

    public double getElectricTotalCostPerYear() { return electricTotalCostPerYear; }
    public void setElectricTotalCostPerYear(double electricTotalCostPerYear) { this.electricTotalCostPerYear = electricTotalCostPerYear; }

    public double getElectricCo2TonsPerYear() { return electricCo2TonsPerYear; }
    public void setElectricCo2TonsPerYear(double electricCo2TonsPerYear) { this.electricCo2TonsPerYear = electricCo2TonsPerYear; }

    public String getDieselModelName() { return dieselModelName; }
    public void setDieselModelName(String dieselModelName) { this.dieselModelName = dieselModelName; }

    public double getDieselCostPerYear() { return dieselCostPerYear; }
    public void setDieselCostPerYear(double dieselCostPerYear) { this.dieselCostPerYear = dieselCostPerYear; }

    public double getDieselMaintenanceCostPerYear() { return dieselMaintenanceCostPerYear; }
    public void setDieselMaintenanceCostPerYear(double dieselMaintenanceCostPerYear) { this.dieselMaintenanceCostPerYear = dieselMaintenanceCostPerYear; }

    public double getDieselTotalCostPerYear() { return dieselTotalCostPerYear; }
    public void setDieselTotalCostPerYear(double dieselTotalCostPerYear) { this.dieselTotalCostPerYear = dieselTotalCostPerYear; }

    public double getDieselCo2TonsPerYear() { return dieselCo2TonsPerYear; }
    public void setDieselCo2TonsPerYear(double dieselCo2TonsPerYear) { this.dieselCo2TonsPerYear = dieselCo2TonsPerYear; }

    public double getAnnualSavingsMXN() { return annualSavingsMXN; }
    public void setAnnualSavingsMXN(double annualSavingsMXN) { this.annualSavingsMXN = annualSavingsMXN; }

    public double getCo2AvoidedTonsPerYear() { return co2AvoidedTonsPerYear; }
    public void setCo2AvoidedTonsPerYear(double co2AvoidedTonsPerYear) { this.co2AvoidedTonsPerYear = co2AvoidedTonsPerYear; }

    public double getSavingsPercent() { return savingsPercent; }
    public void setSavingsPercent(double savingsPercent) { this.savingsPercent = savingsPercent; }
}
