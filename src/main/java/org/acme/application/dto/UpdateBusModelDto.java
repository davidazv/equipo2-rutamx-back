package org.acme.application.dto;

import java.math.BigDecimal;

public class UpdateBusModelDto {

    private String name;
    private BigDecimal autonomyKm;
    private Integer passengerCapacity;
    private BigDecimal unitCostUsd;

    public UpdateBusModelDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAutonomyKm() { return autonomyKm; }
    public void setAutonomyKm(BigDecimal autonomyKm) { this.autonomyKm = autonomyKm; }

    public Integer getPassengerCapacity() { return passengerCapacity; }
    public void setPassengerCapacity(Integer passengerCapacity) { this.passengerCapacity = passengerCapacity; }

    public BigDecimal getUnitCostUsd() { return unitCostUsd; }
    public void setUnitCostUsd(BigDecimal unitCostUsd) { this.unitCostUsd = unitCostUsd; }
}
