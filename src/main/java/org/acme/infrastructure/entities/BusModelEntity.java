package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import org.acme.domain.models.FuelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_models")
public class BusModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "manufacturer", length = 150)
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Column(name = "autonomy_km", precision = 8, scale = 2)
    private BigDecimal autonomyKm;

    @Column(name = "passenger_capacity")
    private Integer passengerCapacity;

    @Column(name = "unit_cost_usd", precision = 12, scale = 2)
    private BigDecimal unitCostUsd;

    @Column(name = "battery_capacity_kwh", precision = 8, scale = 2)
    private BigDecimal batteryCapacityKwh;

    @Column(name = "energy_consumption_kwh_km", precision = 6, scale = 4)
    private BigDecimal energyConsumptionKwhKm;

    @Column(name = "fuel_consumption_l_km", precision = 6, scale = 4)
    private BigDecimal fuelConsumptionLKm;

    @Column(name = "maintenance_cost_per_km", precision = 8, scale = 4)
    private BigDecimal maintenanceCostPerKm;

    @Column(name = "co2_emissions_g_km", precision = 8, scale = 2)
    private BigDecimal co2EmissionsGKm;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BusModelEntity() {}

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
