package org.acme.domain.models;

import java.util.List;

public class ComparativeReport {

    private String routeId;
    private String routeName;
    private double distanceKm;
    private int numberOfBuses;
    private int projectionYears;

    private String electricModelName;
    private String dieselModelName;

    private double electricCostPerYear;
    private double dieselCostPerYear;
    private double electricMaintenancePerYear;
    private double dieselMaintenancePerYear;
    private double co2AvoidedTonsPerYear;

    private double totalInvestmentMXN;
    private double netAnnualSavings;
    private double roiPercent;
    private double paybackYears;

    private List<TcoDataPoint> tcoProjection;
    private int paybackYear;

    public ComparativeReport() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getNumberOfBuses() { return numberOfBuses; }
    public void setNumberOfBuses(int numberOfBuses) { this.numberOfBuses = numberOfBuses; }

    public int getProjectionYears() { return projectionYears; }
    public void setProjectionYears(int projectionYears) { this.projectionYears = projectionYears; }

    public String getElectricModelName() { return electricModelName; }
    public void setElectricModelName(String electricModelName) { this.electricModelName = electricModelName; }

    public String getDieselModelName() { return dieselModelName; }
    public void setDieselModelName(String dieselModelName) { this.dieselModelName = dieselModelName; }

    public double getElectricCostPerYear() { return electricCostPerYear; }
    public void setElectricCostPerYear(double electricCostPerYear) { this.electricCostPerYear = electricCostPerYear; }

    public double getDieselCostPerYear() { return dieselCostPerYear; }
    public void setDieselCostPerYear(double dieselCostPerYear) { this.dieselCostPerYear = dieselCostPerYear; }

    public double getElectricMaintenancePerYear() { return electricMaintenancePerYear; }
    public void setElectricMaintenancePerYear(double electricMaintenancePerYear) { this.electricMaintenancePerYear = electricMaintenancePerYear; }

    public double getDieselMaintenancePerYear() { return dieselMaintenancePerYear; }
    public void setDieselMaintenancePerYear(double dieselMaintenancePerYear) { this.dieselMaintenancePerYear = dieselMaintenancePerYear; }

    public double getCo2AvoidedTonsPerYear() { return co2AvoidedTonsPerYear; }
    public void setCo2AvoidedTonsPerYear(double co2AvoidedTonsPerYear) { this.co2AvoidedTonsPerYear = co2AvoidedTonsPerYear; }

    public double getTotalInvestmentMXN() { return totalInvestmentMXN; }
    public void setTotalInvestmentMXN(double totalInvestmentMXN) { this.totalInvestmentMXN = totalInvestmentMXN; }

    public double getNetAnnualSavings() { return netAnnualSavings; }
    public void setNetAnnualSavings(double netAnnualSavings) { this.netAnnualSavings = netAnnualSavings; }

    public double getRoiPercent() { return roiPercent; }
    public void setRoiPercent(double roiPercent) { this.roiPercent = roiPercent; }

    public double getPaybackYears() { return paybackYears; }
    public void setPaybackYears(double paybackYears) { this.paybackYears = paybackYears; }

    public List<TcoDataPoint> getTcoProjection() { return tcoProjection; }
    public void setTcoProjection(List<TcoDataPoint> tcoProjection) { this.tcoProjection = tcoProjection; }

    public int getPaybackYear() { return paybackYear; }
    public void setPaybackYear(int paybackYear) { this.paybackYear = paybackYear; }
}
