package org.acme.application.usecase;

import org.acme.domain.models.AgencyWithColors;
import org.acme.domain.repository.AgencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListAgenciesWithColorsUseCaseTest {

    private AgencyRepository agencyRepository;
    private ListAgenciesWithColorsUseCase useCase;

    @BeforeEach
    void setUp() {
        agencyRepository = mock(AgencyRepository.class);
        useCase = new ListAgenciesWithColorsUseCase(agencyRepository);
    }

    @Test
    void executeShouldReturnAgenciesFromRepository() {
        AgencyWithColors awc = new AgencyWithColors();
        awc.setAgencyId("SEMOVI");
        awc.setAgencyName("SEMOVI");
        awc.setSampleRouteColors(List.of("#FF0000", "#00FF00"));
        awc.setMultiColor(true);

        when(agencyRepository.findAllWithColorInfo()).thenReturn(List.of(awc));

        List<AgencyWithColors> result = useCase.execute();

        assertEquals(1, result.size());
        assertEquals("SEMOVI", result.get(0).getAgencyId());
        assertTrue(result.get(0).isMultiColor());
        assertEquals(2, result.get(0).getSampleRouteColors().size());
        verify(agencyRepository).findAllWithColorInfo();
    }

    @Test
    void executeShouldReturnEmptyListWhenNoAgencies() {
        when(agencyRepository.findAllWithColorInfo()).thenReturn(Collections.emptyList());

        List<AgencyWithColors> result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(agencyRepository).findAllWithColorInfo();
    }
}
