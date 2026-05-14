package org.acme.application.usecase;

import org.acme.application.dto.UpdateBusModelDto;
import org.acme.application.exception.BusModelNotFoundException;
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

class UpdateBusModelUseCaseTest {

    private BusModelRepository busModelRepository;
    private UpdateBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        useCase = new UpdateBusModelUseCase(busModelRepository);
    }

    private BusModel sampleModel() {
        BusModel m = new BusModel();
        m.setId(1L);
        m.setName("Yutong E12PRO");
        m.setFuelType(FuelType.ELECTRIC);
        m.setAutonomyKm(new BigDecimal("300.00"));
        m.setPassengerCapacity(85);
        m.setUnitCostUsd(new BigDecimal("420000.00"));
        return m;
    }

    @Test
    void shouldUpdateAllEditableFields() {
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(sampleModel()));

        BusModel updated = sampleModel();
        updated.setName("Nuevo Nombre");
        updated.setAutonomyKm(new BigDecimal("350.00"));
        updated.setPassengerCapacity(90);
        updated.setUnitCostUsd(new BigDecimal("450000.00"));
        when(busModelRepository.update(any())).thenReturn(updated);

        UpdateBusModelDto dto = new UpdateBusModelDto();
        dto.setName("Nuevo Nombre");
        dto.setAutonomyKm(new BigDecimal("350.00"));
        dto.setPassengerCapacity(90);
        dto.setUnitCostUsd(new BigDecimal("450000.00"));

        BusModel result = useCase.execute(1L, dto);

        assertEquals("Nuevo Nombre", result.getName());
        assertEquals(new BigDecimal("350.00"), result.getAutonomyKm());
        assertEquals(90, result.getPassengerCapacity());
        assertEquals(new BigDecimal("450000.00"), result.getUnitCostUsd());
        verify(busModelRepository).update(any());
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(sampleModel()));

        BusModel updated = sampleModel();
        updated.setName("Solo Nombre");
        when(busModelRepository.update(any())).thenReturn(updated);

        UpdateBusModelDto dto = new UpdateBusModelDto();
        dto.setName("Solo Nombre");

        BusModel result = useCase.execute(1L, dto);

        assertEquals("Solo Nombre", result.getName());
        verify(busModelRepository).update(any());
    }

    @Test
    void shouldThrowWhenModelNotFound() {
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateBusModelDto dto = new UpdateBusModelDto();
        dto.setName("No existe");

        assertThrows(BusModelNotFoundException.class, () -> useCase.execute(999L, dto));
        verify(busModelRepository, never()).update(any());
    }

    @Test
    void shouldPassCorrectIdToRepository() {
        when(busModelRepository.findById(5L)).thenReturn(Optional.of(sampleModel()));
        when(busModelRepository.update(any())).thenReturn(sampleModel());

        UpdateBusModelDto dto = new UpdateBusModelDto();
        dto.setAutonomyKm(new BigDecimal("200.00"));

        useCase.execute(5L, dto);

        verify(busModelRepository).update(argThat(m -> m.getId().equals(5L)));
    }
}
