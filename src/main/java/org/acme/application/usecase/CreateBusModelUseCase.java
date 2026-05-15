package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.dto.CreateBusModelDto;
import org.acme.application.exception.BusModelAlreadyExistsException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;

import java.math.BigDecimal;

@ApplicationScoped
public class CreateBusModelUseCase {

    private final BusModelRepository busModelRepository;

    @Inject
    public CreateBusModelUseCase(BusModelRepository busModelRepository) {
        this.busModelRepository = busModelRepository;
    }

    public BusModel execute(CreateBusModelDto dto) {
        busModelRepository.findByName(dto.getName()).ifPresent(existing -> {
            throw new BusModelAlreadyExistsException(dto.getName());
        });

        BusModel model = new BusModel();
        model.setName(dto.getName());
        model.setManufacturer(dto.getManufacturer());
        model.setFuelType(FuelType.valueOf(dto.getFuelType().toUpperCase()));
        model.setAutonomyKm(dto.getAutonomyKm());
        model.setPassengerCapacity(dto.getPassengerCapacity());
        model.setUnitCostUsd(dto.getUnitCostUsd());
        model.setBatteryCapacityKwh(dto.getBatteryCapacityKwh() != null ? dto.getBatteryCapacityKwh() : BigDecimal.ZERO);
        model.setEnergyConsumptionKwhKm(dto.getEnergyConsumptionKwhKm() != null ? dto.getEnergyConsumptionKwhKm() : BigDecimal.ZERO);
        model.setFuelConsumptionLKm(dto.getFuelConsumptionLKm() != null ? dto.getFuelConsumptionLKm() : BigDecimal.ZERO);
        model.setMaintenanceCostPerKm(dto.getMaintenanceCostPerKm() != null ? dto.getMaintenanceCostPerKm() : BigDecimal.ZERO);
        model.setCo2EmissionsGKm(dto.getCo2EmissionsGKm() != null ? dto.getCo2EmissionsGKm() : BigDecimal.ZERO);

        return busModelRepository.create(model);
    }
}
