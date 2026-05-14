package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.repository.BusModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteBusModelUseCaseTest {

    private BusModelRepository busModelRepository;
    private DeleteBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        useCase = new DeleteBusModelUseCase(busModelRepository);
    }

    @Test
    void shouldDeleteExistingModel() {
        BusModel model = new BusModel();
        model.setId(1L);
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));

        useCase.execute(1L);

        verify(busModelRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenModelNotFound() {
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class, () -> useCase.execute(999L));
        verify(busModelRepository, never()).deleteById(any());
    }

    @Test
    void shouldPassCorrectIdToRepository() {
        BusModel model = new BusModel();
        model.setId(3L);
        when(busModelRepository.findById(3L)).thenReturn(Optional.of(model));

        useCase.execute(3L);

        verify(busModelRepository).deleteById(3L);
    }
}
