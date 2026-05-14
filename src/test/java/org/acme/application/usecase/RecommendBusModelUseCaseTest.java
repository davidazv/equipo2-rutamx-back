package org.acme.application.usecase;

import org.acme.application.exception.NoDemandDataException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.BusModelRecommendation;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.RouteTimeComparison;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendBusModelUseCaseTest {

    private RouteRepository routeRepository;
    private AfluenciaMetrobusRepository afluenciaRepository;
    private BusModelRepository busModelRepository;
    private RecommendBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        useCase = new RecommendBusModelUseCase(routeRepository, afluenciaRepository, busModelRepository);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RouteTimeComparison buildRoute(String routeId, String shortName,
                                           double distanceKm, int frequencyMinutes) {
        RouteTimeComparison r = new RouteTimeComparison();
        r.setRouteId(routeId);
        r.setRouteShortName(shortName);
        r.setRouteLongName("Ruta " + shortName);
        r.setDistanceKm(distanceKm);
        r.setFrequencyMinutes(frequencyMinutes);
        return r;
    }

    private BusModel buildModel(Long id, int capacity, double autonomyKm, double costUsd) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName("Bus-" + id);
        m.setManufacturer("TestMfg");
        m.setFuelType(FuelType.ELECTRIC);
        m.setPassengerCapacity(capacity);
        m.setAutonomyKm(BigDecimal.valueOf(autonomyKm));
        m.setUnitCostUsd(BigDecimal.valueOf(costUsd));
        return m;
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    void executeShouldReturnRecommendationForAllDayTypes() {
        // freq=3min → busesPerHour=20; weekday demand=5000
        // peakHour = round(5000 * 0.12) = 600
        // reqCap   = ceil(600 / (20 * 0.80)) = ceil(37.5) = 38
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 3000.0, "sunday", 2000.0));
        when(busModelRepository.findAll())
                .thenReturn(List.of(buildModel(1L, 85, 300, 420_000)));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        assertNotNull(result.getRecommendations().getWeekday());
        assertNotNull(result.getRecommendations().getSaturday());
        assertNotNull(result.getRecommendations().getSunday());
        assertEquals(38, result.getRecommendations().getWeekday().getRequiredCapacity());
        assertEquals("route-1", result.getRouteId());
    }

    @Test
    void executeShouldComputePeakHourDemandFromDailyAverage() {
        // weekday=10000 → peakHour = round(10000 * 0.12) = 1200
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 10_000.0, "saturday", 0.0, "sunday", 0.0));
        when(busModelRepository.findAll()).thenReturn(List.of());

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        assertEquals(1200L, result.getRecommendations().getWeekday().getPeakHourDemand());
    }

    @Test
    void executeShouldUseDefaultBusesPerHourWhenFrequencyIsZero() {
        // freq=0 → DEFAULT_BUSES_PER_HOUR=10; demand=5000
        // reqCap = ceil(600 / (10 * 0.80)) = ceil(75) = 75
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 0.0, "sunday", 0.0));
        when(busModelRepository.findAll())
                .thenReturn(List.of(buildModel(1L, 85, 300, 420_000)));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        assertEquals(75, result.getRecommendations().getWeekday().getRequiredCapacity());
    }

    // ── Ranking ──────────────────────────────────────────────────────────────

    @Test
    void executeShouldMarkCheapestEligibleModelAsRecommended() {
        // Both meet reqCap=38 and minAutonomy=60km; cheaper one (id=2) wins
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 0.0, "sunday", 0.0));

        BusModel expensive = buildModel(1L, 90, 300, 600_000);
        BusModel cheap     = buildModel(2L, 80, 300, 300_000);
        when(busModelRepository.findAll()).thenReturn(List.of(expensive, cheap));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        var models = result.getRecommendations().getWeekday().getModels();
        assertTrue(models.stream().filter(m -> m.getModel().getId() == 2L).findFirst().orElseThrow().isRecommended());
        assertFalse(models.stream().filter(m -> m.getModel().getId() == 1L).findFirst().orElseThrow().isRecommended());
    }

    @Test
    void executeShouldMarkNoModelAsRecommendedWhenNoneEligible() {
        // demand=100000 → peakHour=12000, freq=0 → busesPerHour=10
        // reqCap = ceil(12000/(10*0.80)) = 1500 → cap=50 too small
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 100_000.0, "saturday", 0.0, "sunday", 0.0));
        when(busModelRepository.findAll())
                .thenReturn(List.of(buildModel(1L, 50, 300, 200_000)));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        assertTrue(result.getRecommendations().getWeekday().getModels()
                .stream().noneMatch(m -> m.isRecommended()));
    }

    @Test
    void executeShouldExcludeModelsWithInsufficientAutonomy() {
        // distance=100km → minAutonomy=200km; shortRange (150km) not eligible
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 100, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 0.0, "sunday", 0.0));

        BusModel shortRange = buildModel(1L, 90, 150, 300_000);
        BusModel longRange  = buildModel(2L, 90, 300, 400_000);
        when(busModelRepository.findAll()).thenReturn(List.of(shortRange, longRange));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        var models = result.getRecommendations().getWeekday().getModels();
        assertFalse(models.stream().filter(m -> m.getModel().getId() == 1L).findFirst().orElseThrow().isMeetsAutonomy());
        assertTrue(models.stream().filter(m -> m.getModel().getId() == 2L).findFirst().orElseThrow().isRecommended());
    }

    @Test
    void executeShouldSetRankStartingAtOne() {
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 0.0, "sunday", 0.0));
        when(busModelRepository.findAll())
                .thenReturn(List.of(buildModel(1L, 85, 300, 420_000),
                                    buildModel(2L, 90, 300, 500_000)));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        var models = result.getRecommendations().getWeekday().getModels();
        assertEquals(1, models.get(0).getRank());
        assertEquals(2, models.get(1).getRank());
    }

    @Test
    void executeShouldIncludeJustificationOnEachModel() {
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0, "saturday", 0.0, "sunday", 0.0));
        when(busModelRepository.findAll())
                .thenReturn(List.of(buildModel(1L, 85, 300, 420_000)));

        BusModelRecommendation result = useCase.execute("route-1", 0.80);

        assertNotNull(result.getRecommendations().getWeekday().getModels().get(0).getJustification());
    }

    // ── Error cases ──────────────────────────────────────────────────────────

    @Test
    void executeShouldThrowRouteNotFoundExceptionWhenRouteDoesNotExist() {
        when(routeRepository.findByIdWithTimeComparison("NONEXISTENT"))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("NONEXISTENT", 0.80));
    }

    @Test
    void executeShouldThrowNoDemandDataExceptionWhenDemandMapIsEmpty() {
        when(routeRepository.findByIdWithTimeComparison("route-1"))
                .thenReturn(Optional.of(buildRoute("route-1", "1", 30, 3)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of());

        assertThrows(NoDemandDataException.class,
                () -> useCase.execute("route-1", 0.80));
    }
}
