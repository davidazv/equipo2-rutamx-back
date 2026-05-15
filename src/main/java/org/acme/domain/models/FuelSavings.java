package org.acme.domain.models;

public class FuelSavings {

    private String routeId;
    private double routeDistanceKm;
    private long busModelId;
    private String busModelName;
    private int numberOfBuses;
    private double fuelSavingsMXN;
    private double fuelSavingsLiters;
    private double dieselReferencePriceMXN;
    private double dieselConsumptionLKm;
    private double dieselCostPerYear;
    private double electricCostPerYear;
    private int projectionYears;

    public FuelSavings() {}

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public long getBusModelId() { return busModelId; }
    public void setBusModelId(long busModelId) { this.busModelId = busModelId; }

    public String getBusModelName() { return busModelName; }
    public void setBusModelName(String busModelName) { this.busModelName = busModelName; }

    public int getNumberOfBuses() { return numberOfBuses; }
    public void setNumberOfBuses(int numberOfBuses) { this.numberOfBuses = numberOfBuses; }

    public double getFuelSavingsMXN() { return fuelSavingsMXN; }
    public void setFuelSavingsMXN(double fuelSavingsMXN) { this.fuelSavingsMXN = fuelSavingsMXN; }

    public double getFuelSavingsLiters() { return fuelSavingsLiters; }
    public void setFuelSavingsLiters(double fuelSavingsLiters) { this.fuelSavingsLiters = fuelSavingsLiters; }

    public double getDieselReferencePriceMXN() { return dieselReferencePriceMXN; }
    public void setDieselReferencePriceMXN(double dieselReferencePriceMXN) { this.dieselReferencePriceMXN = dieselReferencePriceMXN; }

    public double getDieselConsumptionLKm() { return dieselConsumptionLKm; }
    public void setDieselConsumptionLKm(double dieselConsumptionLKm) { this.dieselConsumptionLKm = dieselConsumptionLKm; }

    public double getDieselCostPerYear() { return dieselCostPerYear; }
    public void setDieselCostPerYear(double dieselCostPerYear) { this.dieselCostPerYear = dieselCostPerYear; }

    public double getElectricCostPerYear() { return electricCostPerYear; }
    public void setElectricCostPerYear(double electricCostPerYear) { this.electricCostPerYear = electricCostPerYear; }

    public int getProjectionYears() { return projectionYears; }
    public void setProjectionYears(int projectionYears) { this.projectionYears = projectionYears; }
}
