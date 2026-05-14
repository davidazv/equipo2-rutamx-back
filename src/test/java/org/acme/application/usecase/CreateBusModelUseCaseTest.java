package org.acme.application.usecase;

import org.acme.application.dto.CreateBusModelDto;
import org.acme.application.exception.BusModelAlreadyExistsException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateBusModelUseCaseTest {

    private BusModelRepository busModelRepository;
    private CreateBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        useCase = new CreateBusModelUseCase(busModelRepository);
    }

    private CreateBusModelDto validDto() {
        CreateBusModelDto dto = new CreateBusModelDto();
        dto.setName("BYD K9M");
        dto.setManufacturer("BYD");
        dto.setFuelType("ELECTRIC");
        dto.setAutonomyKm(new BigDecimal("250.00"));
        dto.setPassengerCapacity(75);
        dto.setUnitCostUsd(new BigDecimal("380000.00"));
        dto.setBatteryCapacityKwh(new BigDecimal("324.00"));
        dto.setEnergyConsumptionKwhKm(new BigDecimal("1.2"));
        dto.setFuelConsumptionLKm(BigDecimal.ZERO);
        dto.setMaintenanceCostPerKm(new BigDecimal("0.10"));
        dto.setCo2EmissionsGKm(BigDecimal.ZERO);
        return dto;
    }

    @Test
    void shouldCreateModelWithAllFields() {
        when(busModelRepository.findByName("BYD K9M")).thenReturn(Optional.empty());

        BusModel saved = new BusModel();
        saved.setId(7L);
        saved.setName("BYD K9M");
        saved.setFuelType(FuelType.ELECTRIC);
        when(busModelRepository.create(any())).thenReturn(saved);

        BusModel result = useCase.execute(validDto());

        assertEquals("BYD K9M", result.getName());
        assertEquals(7L, result.getId());
        verify(busModelRepository).create(any());
    }

    @Test
    void shouldThrowWhenNameAlreadyExists() {
        BusModel existing = new BusModel();
        existing.setName("BYD K9M");
        when(busModelRepository.findByName("BYD K9M")).thenReturn(Optional.of(existing));

        assertThrows(BusModelAlreadyExistsException.class, () -> useCase.execute(validDto()));
        verify(busModelRepository, never()).create(any());
    }

    @Test
    void shouldDefaultNullNumericFieldsToZero() {
        CreateBusModelDto dto = validDto();
        dto.setBatteryCapacityKwh(null);
        dto.setEnergyConsumptionKwhKm(null);
        dto.setFuelConsumptionLKm(null);
        dto.setMaintenanceCostPerKm(null);
        dto.setCo2EmissionsGKm(null);

        when(busModelRepository.findByName(any())).thenReturn(Optional.empty());
        when(busModelRepository.create(any())).thenAnswer(inv -> inv.getArgument(0));

        BusModel result = useCase.execute(dto);

        assertEquals(BigDecimal.ZERO, result.getBatteryCapacityKwh());
        assertEquals(BigDecimal.ZERO, result.getEnergyConsumptionKwhKm());
        assertEquals(BigDecimal.ZERO, result.getFuelConsumptionLKm());
        assertEquals(BigDecimal.ZERO, result.getMaintenanceCostPerKm());
        assertEquals(BigDecimal.ZERO, result.getCo2EmissionsGKm());
    }

    @Test
    void shouldThrowOnInvalidFuelType() {
        CreateBusModelDto dto = validDto();
        dto.setFuelType("GASOLINA");

        when(busModelRepository.findByName(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(dto));
        verify(busModelRepository, never()).create(any());
    }
}
