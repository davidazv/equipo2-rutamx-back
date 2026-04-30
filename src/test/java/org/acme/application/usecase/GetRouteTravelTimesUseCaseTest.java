package org.acme.application.usecase;

import org.acme.domain.models.RouteTimeComparison;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRouteTravelTimesUseCaseTest {

    private RouteRepository routeRepository;
    private GetRouteTravelTimesUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        useCase = new GetRouteTravelTimesUseCase(routeRepository);
    }

    private RouteTimeComparison buildRoute(String id, double distanceKm, int scheduledMin, int freqMin) {
        RouteTimeComparison r = new RouteTimeComparison();
        r.setRouteId(id);
        r.setAgencyId("SEMOVI");
        r.setRouteShortName("13");
        r.setRouteLongName("Trolebus Linea 13");
        r.setDistanceKm(distanceKm);
        r.setScheduledTimeMinutes(scheduledMin);
        r.setFrequencyMinutes(freqMin);
        return r;
    }

    @Test
    void executeShouldReturnEmptyListWhenRepositoryReturnsEmpty() {
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of());

        List<RouteTimeComparison> result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(routeRepository).findAllWithTimeComparison();
    }

    @Test
    void executeShouldCallRepositoryFindAllWithTimeComparison() {
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of());

        useCase.execute();

        verify(routeRepository, times(1)).findAllWithTimeComparison();
    }

    @Test
    void executeShouldComputeEstimatedTimeAsDistanceOverReferenceSpeed() {
        // 20 km / 20 km/h * 60 = 60 min
        RouteTimeComparison route = buildRoute("R1", 20.0, 60, 5);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(60, result.get(0).getEstimatedTimeMinutes());
    }

    @Test
    void executeShouldRoundEstimatedTimeToNearestMinute() {
        // 28.5 km / 20 km/h * 60 = 85.5 → rounds to 86
        RouteTimeComparison route = buildRoute("R1", 28.5, 79, 3);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(86, result.get(0).getEstimatedTimeMinutes());
    }

    @Test
    void executeShouldComputeAvgSpeedFromScheduledTime() {
        // 28.5 km / (79/60 h) = 21.6455... → rounded to 21.6
        RouteTimeComparison route = buildRoute("R1", 28.5, 79, 3);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(21.6, result.get(0).getAvgSpeedKmH(), 0.001);
    }

    @Test
    void executeShouldComputePositiveVariabilityWhenEstimatedExceedsScheduled() {
        // 28.5 km, 79 min scheduled → estimatedMin=86 → (86-79)/79*100 = 8.860... → 8.9
        RouteTimeComparison route = buildRoute("R1", 28.5, 79, 3);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(8.9, result.get(0).getVariabilityPercent(), 0.001);
    }

    @Test
    void executeShouldComputeNegativeVariabilityWhenScheduledExceedsEstimated() {
        // 20 km, 79 min scheduled → estimatedMin=60 → (60-79)/79*100 = -24.050... → -24.1
        RouteTimeComparison route = buildRoute("R1", 20.0, 79, 0);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(-24.1, result.get(0).getVariabilityPercent(), 0.001);
    }

    @Test
    void executeShouldComputeZeroVariabilityWhenEstimatedEqualsScheduled() {
        // 20 km, 60 min scheduled → estimatedMin=60 → (60-60)/60*100 = 0.0
        RouteTimeComparison route = buildRoute("R1", 20.0, 60, 5);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(0.0, result.get(0).getVariabilityPercent(), 0.001);
    }

    @Test
    void executeShouldSetAvgSpeedToZeroWhenScheduledTimeIsZero() {
        RouteTimeComparison route = buildRoute("R1", 20.0, 0, 0);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(0.0, result.get(0).getAvgSpeedKmH(), 0.001);
    }

    @Test
    void executeShouldSetVariabilityToZeroWhenScheduledTimeIsZero() {
        RouteTimeComparison route = buildRoute("R1", 20.0, 0, 0);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(route));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(0.0, result.get(0).getVariabilityPercent(), 0.001);
    }

    @Test
    void executeShouldProcessAllRoutesInList() {
        // R1: 20 km, 60 min → estimatedMin=60
        // R2: 15 km, 0 min → estimatedMin=45
        RouteTimeComparison r1 = buildRoute("R1", 20.0, 60, 5);
        RouteTimeComparison r2 = buildRoute("R2", 15.0, 0, 0);
        when(routeRepository.findAllWithTimeComparison()).thenReturn(List.of(r1, r2));

        List<RouteTimeComparison> result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals(60, result.get(0).getEstimatedTimeMinutes());
        assertEquals(45, result.get(1).getEstimatedTimeMinutes());
        assertEquals(0.0, result.get(1).getAvgSpeedKmH(), 0.001);
        assertEquals(0.0, result.get(1).getVariabilityPercent(), 0.001);
    }
}
