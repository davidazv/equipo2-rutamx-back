package org.acme.domain.models;

import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Modelo de autobús (eléctrico o diésel)")
public class BusModel {

    @Schema(description = "ID único del modelo", example = "1", readOnly = true)
    private Long id;

    @Schema(description = "Nombre comercial del modelo", example = "Yutong E12")
    private String name;

    @Schema(description = "Fabricante", example = "Yutong")
    private String manufacturer;

    @Schema(description = "Tipo de combustible: ELECTRIC o DIESEL")
    private FuelType fuelType;

    @Schema(description = "Autonomía en kilómetros", example = "300.00")
    private BigDecimal autonomyKm;

    @Schema(description = "Capacidad de pasajeros", example = "90")
    private Integer passengerCapacity;

    @Schema(description = "Costo unitario en USD", example = "350000.00")
    private BigDecimal unitCostUsd;

    @Schema(description = "Capacidad de batería en kWh (solo eléctricos)", example = "281.00")
    private BigDecimal batteryCapacityKwh;

    @Schema(description = "Consumo energético en kWh/km (solo eléctricos)", example = "0.94")
    private BigDecimal energyConsumptionKwhKm;

    @Schema(description = "Consumo de combustible en L/km (solo diésel)", example = "0.35")
    private BigDecimal fuelConsumptionLKm;

    @Schema(description = "Costo de mantenimiento por km en USD", example = "0.15")
    private BigDecimal maintenanceCostPerKm;

    @Schema(description = "Emisiones CO2 en g/km (0 para eléctricos)", example = "0.00")
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
