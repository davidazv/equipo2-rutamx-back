package org.acme.domain.models;

import java.math.BigDecimal;

public class BusModel {

    private Long id;
    private String name;
    private String manufacturer;
    private FuelType fuelType;
    private BigDecimal autonomyKm;
    private Integer passengerCapacity;
    private BigDecimal unitCostUsd;
    private BigDecimal batteryCapacityKwh;
    private BigDecimal energyConsumptionKwhKm;
    private BigDecimal fuelConsumptionLKm;
    private BigDecimal maintenanceCostPerKm;
    private BigDecimal co2EmissionsGKm;

    public BusModel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public BigDecimal getAutonomyKm() { return autonomyKm; }
    public void setAutonomyKm(BigDecimal autonomyKm) { this.autonomyKm = autonomyKm; }

    public Integer getPassengerCapacity() { return passengerCapacity; }
    public void setPassengerCapacity(Integer passengerCapacity) { this.passengerCapacity = passengerCapacity; }

    public BigDecimal getUnitCostUsd() { return unitCostUsd; }
    public void setUnitCostUsd(BigDecimal unitCostUsd) { this.unitCostUsd = unitCostUsd; }

    public BigDecimal getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(BigDecimal batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public BigDecimal getEnergyConsumptionKwhKm() { return energyConsumptionKwhKm; }
    public void setEnergyConsumptionKwhKm(BigDecimal energyConsumptionKwhKm) { this.energyConsumptionKwhKm = energyConsumptionKwhKm; }

    public BigDecimal getFuelConsumptionLKm() { return fuelConsumptionLKm; }
    public void setFuelConsumptionLKm(BigDecimal fuelConsumptionLKm) { this.fuelConsumptionLKm = fuelConsumptionLKm; }

    public BigDecimal getMaintenanceCostPerKm() { return maintenanceCostPerKm; }
    public void setMaintenanceCostPerKm(BigDecimal maintenanceCostPerKm) { this.maintenanceCostPerKm = maintenanceCostPerKm; }

    public BigDecimal getCo2EmissionsGKm() { return co2EmissionsGKm; }
    public void setCo2EmissionsGKm(BigDecimal co2EmissionsGKm) { this.co2EmissionsGKm = co2EmissionsGKm; }
}
