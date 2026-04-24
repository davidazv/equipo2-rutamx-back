package org.acme.infrastructure.mapper;

import org.acme.domain.models.BusModel;
import org.acme.infrastructure.entities.BusModelEntity;

public class BusModelMapper {

    private BusModelMapper() {}

    public static BusModel toDomain(BusModelEntity entity) {
        BusModel model = new BusModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setManufacturer(entity.getManufacturer());
        model.setFuelType(entity.getFuelType());
        model.setAutonomyKm(entity.getAutonomyKm());
        model.setPassengerCapacity(entity.getPassengerCapacity());
        model.setUnitCostUsd(entity.getUnitCostUsd());
        model.setBatteryCapacityKwh(entity.getBatteryCapacityKwh());
        model.setEnergyConsumptionKwhKm(entity.getEnergyConsumptionKwhKm());
        model.setFuelConsumptionLKm(entity.getFuelConsumptionLKm());
        model.setMaintenanceCostPerKm(entity.getMaintenanceCostPerKm());
        model.setCo2EmissionsGKm(entity.getCo2EmissionsGKm());
        return model;
    }

    public static BusModelEntity toEntity(BusModel model) {
        BusModelEntity entity = new BusModelEntity();
        entity.setName(model.getName());
        entity.setManufacturer(model.getManufacturer());
        entity.setFuelType(model.getFuelType());
        entity.setAutonomyKm(model.getAutonomyKm());
        entity.setPassengerCapacity(model.getPassengerCapacity());
        entity.setUnitCostUsd(model.getUnitCostUsd());
        entity.setBatteryCapacityKwh(model.getBatteryCapacityKwh());
        entity.setEnergyConsumptionKwhKm(model.getEnergyConsumptionKwhKm());
        entity.setFuelConsumptionLKm(model.getFuelConsumptionLKm());
        entity.setMaintenanceCostPerKm(model.getMaintenanceCostPerKm());
        entity.setCo2EmissionsGKm(model.getCo2EmissionsGKm());
        return entity;
    }
}
