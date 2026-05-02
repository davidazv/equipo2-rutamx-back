package org.acme.application.usecase;

import org.acme.application.exception.NoGtfsDataException;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.models.TripsByDayResult;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTripsByDayUseCaseTest {

    private TripRepository tripRepository;
    private AfluenciaMetrobusRepository afluenciaRepository;
    private GetTripsByDayUseCase useCase;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        useCase = new GetTripsByDayUseCase(tripRepository, afluenciaRepository);
    }

    private RouteTripsPerDay buildRouteTrips(String routeId, String shortName, String color,
                                              int mon, int tue, int wed, int thu, int fri, int sat, int sun) {
        RouteTripsPerDay t = new RouteTripsPerDay();
        t.setRouteId(routeId);
        t.setRouteShortName(shortName);
        t.setRouteLongName("Route " + routeId);
        t.setAgencyId("AGENCY");
        t.setAgencyColor(color);
        t.setMonday(mon);
        t.setTuesday(tue);
        t.setWednesday(wed);
        t.setThursday(thu);
        t.setFriday(fri);
        t.setSaturday(sat);
        t.setSunday(sun);
        return t;
    }

    @Test
    void executeShouldThrowNoGtfsDataWhenNoTrips() {
        when(tripRepository.findCountGroupedByRouteAndDay()).thenReturn(Collections.emptyList());
        assertThrows(NoGtfsDataException.class, () -> useCase.execute());
    }

    @Test
    void executeShouldReturnResultForEachRoute() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(
                        buildRouteTrips("R1", "1", "FF0000", 5, 5, 5, 5, 5, 3, 3),
                        buildRouteTrips("R2", "2", "00FF00", 4, 4, 4, 4, 4, 2, 2)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<TripsByDayResult> results = useCase.execute();
        assertEquals(2, results.size());
    }

    @Test
    void executeShouldComputeTotalSemanal() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "1", "AAA", 5, 5, 5, 5, 5, 3, 2)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        TripsByDayResult result = useCase.execute().get(0);
        assertEquals(30, result.getTotalSemanal()); // 5+5+5+5+5+3+2
    }

    @Test
    void executeShouldSetCalidadBajaWhenNoAfluencia() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "1", "AAA", 5, 5, 5, 5, 5, 3, 2)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        TripsByDayResult result = useCase.execute().get(0);
        assertEquals("Baja", result.getCalidadDatos());
        assertNull(result.getDemandaDiariaPromedio());
    }

    @Test
    void executeShouldSetCalidadAltaAndDemandaWhenAfluenciaExists() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "13", "AAA", 5, 5, 5, 5, 5, 3, 2)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(List.of(
                new AfluenciaResumen("13", 2, 1000.0),
                new AfluenciaResumen("13", 3, 1200.0)));

        TripsByDayResult result = useCase.execute().get(0);
        assertEquals("Alta", result.getCalidadDatos());
        assertNotNull(result.getDemandaDiariaPromedio());
        // demanda = (1000 + 1200) / 2 = 1100
        assertEquals(1100.0, result.getDemandaDiariaPromedio(), 0.01);
    }

    @Test
    void executeShouldReturnAllDayFields() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "1", "AAA", 5, 4, 3, 2, 1, 6, 7)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        TripsByDayResult result = useCase.execute().get(0);
        assertEquals(5, result.getMonday());
        assertEquals(4, result.getTuesday());
        assertEquals(3, result.getWednesday());
        assertEquals(2, result.getThursday());
        assertEquals(1, result.getFriday());
        assertEquals(6, result.getSaturday());
        assertEquals(7, result.getSunday());
    }

    @Test
    void executeShouldReturnAllRequiredFields() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "1", "009B3A", 5, 5, 5, 5, 5, 3, 3)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        TripsByDayResult result = useCase.execute().get(0);
        assertNotNull(result.getRouteId());
        assertNotNull(result.getRouteName());
        assertNotNull(result.getAgencyColor());
        assertNotNull(result.getCalidadDatos());
        assertTrue(result.getTotalSemanal() > 0);
    }

    @Test
    void executeShouldIgnoreAfluenciaForNonMatchingLinea() {
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildRouteTrips("R1", "13", "AAA", 5, 5, 5, 5, 5, 3, 3)));
        // afluencia for a different linea
        when(afluenciaRepository.findGroupedByLineaAndDow())
                .thenReturn(List.of(new AfluenciaResumen("OTHER_LINE", 2, 999.0)));

        TripsByDayResult result = useCase.execute().get(0);
        assertEquals("Baja", result.getCalidadDatos());
        assertNull(result.getDemandaDiariaPromedio());
    }

    @Test
    void executeShouldPreserveOrderFromRepository() {
        RouteTripsPerDay first = buildRouteTrips("R1", "1", "AAA", 10, 10, 10, 10, 10, 5, 5);
        RouteTripsPerDay second = buildRouteTrips("R2", "2", "BBB", 2, 2, 2, 2, 2, 1, 1);

        when(tripRepository.findCountGroupedByRouteAndDay()).thenReturn(List.of(first, second));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<TripsByDayResult> results = useCase.execute();
        assertEquals("R1", results.get(0).getRouteId());
        assertEquals("R2", results.get(1).getRouteId());
    }
}
