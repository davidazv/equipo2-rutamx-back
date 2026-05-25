package org.acme.application.usecase;

import org.acme.application.exception.NoDemandDataException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.BusModelRecommendation;
import org.acme.domain.models.BusModelRank;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecommendBusModelUseCaseTest {

    private RouteRepository routeRepository;
    private AfluenciaMetrobusRepository afluenciaRepository;
    private BusModelRepository busModelRepository;
    private RecommendBusModelUseCase useCase;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        routeRepository     = mock(RouteRepository.class);
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        busModelRepository  = mock(BusModelRepository.class);
        useCase = new RecommendBusModelUseCase(routeRepository, afluenciaRepository, busModelRepository);
    }

    /** Minimal RouteTimeComparison with only the fields the use case reads. */
    private RouteTimeComparison route(String shortName, double distanceKm, int frequencyMinutes) {
        RouteTimeComparison r = new RouteTimeComparison();
        r.setRouteId("ROUTE_" + shortName);
        r.setRouteShortName(shortName);
        r.setRouteLongName("Ruta de prueba " + shortName);
        r.setAgencyId("TEST");
        r.setDistanceKm(distanceKm);
        r.setFrequencyMinutes(frequencyMinutes);
        return r;
    }

    /** BusModel with explicit capacity and autonomy (both non-null). */
    private BusModel model(long id, int capacityPax, int autonomyKm, int unitCostUsd) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName("Model-" + id);
        m.setManufacturer("Fabricante");
        m.setPassengerCapacity(capacityPax);
        m.setAutonomyKm(BigDecimal.valueOf(autonomyKm));
        m.setUnitCostUsd(BigDecimal.valueOf(unitCostUsd));
        return m;
    }

    /** BusModel with null passengerCapacity. */
    private BusModel modelNullCapacity(long id, int autonomyKm) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName("Model-" + id);
        m.setManufacturer("Fabricante");
        m.setPassengerCapacity(null);
        m.setAutonomyKm(BigDecimal.valueOf(autonomyKm));
        m.setUnitCostUsd(BigDecimal.valueOf(100_000));
        return m;
    }

    /** BusModel with null autonomyKm. */
    private BusModel modelNullAutonomy(long id, int capacityPax) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName("Model-" + id);
        m.setManufacturer("Fabricante");
        m.setPassengerCapacity(capacityPax);
        m.setAutonomyKm(null);
        m.setUnitCostUsd(BigDecimal.valueOf(100_000));
        return m;
    }

    /** Weekday-only demand map. */
    private Map<String, Double> demand(double weekday, double saturday, double sunday) {
        return Map.of("weekday", weekday, "saturday", saturday, "sunday", sunday);
    }

    // ── AC1 — Exception: route not found ─────────────────────────────────────

    @Test
    void executeShouldThrowRouteNotFoundWhenRouteDoesNotExist() {
        when(routeRepository.findByIdWithTimeComparison("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("UNKNOWN", 0.80));
    }

    @Test
    void executeShouldNotQueryAfluenciaWhenRouteDoesNotExist() {
        when(routeRepository.findByIdWithTimeComparison("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> useCase.execute("UNKNOWN", 0.80));

        verify(afluenciaRepository, never()).findAvgDemandByLinea(anyString());
    }

    // ── AC1 — Exception: no demand data ──────────────────────────────────────

    @Test
    void executeShouldThrowNoDemandDataWhenDemandMapIsEmpty() {
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of());

        assertThrows(NoDemandDataException.class,
                () -> useCase.execute("R1", 0.80));
    }

    // ── AC1 — Linea key construction ──────────────────────────────────────────

    @Test
    void executeShouldBuildLineaKeyAsLineaSpaceLowercaseShortName() {
        // shortName "1A" → key "linea 1a"
        when(routeRepository.findByIdWithTimeComparison("R1A"))
                .thenReturn(Optional.of(route("1A", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1a"))
                .thenReturn(demand(1000, 800, 600));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 50, 100, 200_000)));

        useCase.execute("R1A", 0.80);

        verify(afluenciaRepository).findAvgDemandByLinea("linea 1a");
    }

    // ── AC1 — Peak-hour demand calculation ───────────────────────────────────

    @Test
    void executeShouldComputePeakHourDemandAs12PercentOfAvgDailyDemand() {
        // avgWeekday=5000 → peakHour = round(5000 × 0.12) = 600
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 200, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertEquals(600L, result.getRecommendations().getWeekday().getPeakHourDemand());
    }

    // ── AC1 — Frequency: formula vs fallback ─────────────────────────────────

    @Test
    void executeShouldUseFrequencyFormulaWhenFrequencyMinutesIsPositive() {
        // freq=10 → busesPerHour=round(60/10)=6
        // avgWeekday=5000 → peak=600
        // requiredCap = ceil(600 / (6 × 0.80)) = ceil(125) = 125
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 200, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertEquals(125, result.getRecommendations().getWeekday().getRequiredCapacity());
    }

    @Test
    void executeShouldUseFallback10BusesPerHourWhenFrequencyIsZero() {
        // freq=0 → busesPerHour=10
        // avgWeekday=1000 → peak=round(1000×0.12)=120
        // requiredCap = ceil(120 / (10 × 0.80)) = ceil(15) = 15
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(1000, 800, 600));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 100, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertEquals(15, result.getRecommendations().getWeekday().getRequiredCapacity());
    }

    // ── AC1 — Demand summary population ──────────────────────────────────────

    @Test
    void executeShouldPopulateDemandSummaryForAllThreeDayTypes() {
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3300, 2200));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 100, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertEquals(5000L, result.getDemand().getAvgWeekday());
        assertEquals(3300L, result.getDemand().getAvgSaturday());
        assertEquals(2200L, result.getDemand().getAvgSunday());
    }

    @Test
    void executeShouldDefaultMissingDayTypeKeysToZero() {
        // Only "weekday" present in map — saturday/sunday default to 0
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(Map.of("weekday", 5000.0));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 100, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertEquals(0L, result.getDemand().getAvgSaturday());
        assertEquals(0L, result.getDemand().getAvgSunday());
    }

    @Test
    void executeShouldPopulateRecommendationsForAllThreeDayTypes() {
        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 100, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        assertNotNull(result.getRecommendations().getWeekday());
        assertNotNull(result.getRecommendations().getSaturday());
        assertNotNull(result.getRecommendations().getSunday());
    }

    // ── AC1 — Route metadata in result ───────────────────────────────────────

    @Test
    void executeShouldPopulateRouteMetadataFromRepository() {
        RouteTimeComparison r = route("1", 28.5, 3);
        when(routeRepository.findByIdWithTimeComparison("ROUTE_1")).thenReturn(Optional.of(r));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1")).thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(model(1L, 100, 100, 200_000)));

        BusModelRecommendation result = useCase.execute("ROUTE_1", 0.80);

        assertEquals("ROUTE_1",             result.getRouteId());
        assertEquals("1",                   result.getRouteShortName());
        assertEquals("Ruta de prueba 1",    result.getRouteLongName());
        assertEquals(28.5,                  result.getDistanceKm(), 0.001);
        assertEquals(3,                     result.getFrequencyMinutes());
    }

    // ── AC2 — Eligible model is recommended ──────────────────────────────────

    @Test
    void executeShouldMarkFirstEligibleModelAsRecommended() {
        // Route: 20 km → minAutonomy=40 km. freq=10 → busesPerHour=6, peak=600, reqCap=125
        // Model 1: capacity=150 (≥125), autonomy=100km (≥40km) → eligible, cost=100k
        // Model 2: capacity=200 (≥125), autonomy=100km (≥40km) → eligible, cost=200k
        // Model 1 is recommended (lower cost)
        BusModel cheap  = model(1L, 150, 100, 100_000);
        BusModel costly = model(2L, 200, 100, 200_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(costly, cheap)); // unsorted on purpose

        BusModelRecommendation result = useCase.execute("R1", 0.80);
        List<BusModelRank> models = result.getRecommendations().getWeekday().getModels();

        assertEquals(1L, models.get(0).getModel().getId(),
                "cheapest eligible model should be rank 1");
        assertTrue(models.get(0).isRecommended());
        assertFalse(models.get(1).isRecommended());
    }

    @Test
    void executeShouldNotMarkMoreThanOneModelAsRecommended() {
        BusModel m1 = model(1L, 150, 100, 100_000);
        BusModel m2 = model(2L, 200, 100, 200_000);
        BusModel m3 = model(3L, 300, 100, 300_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m1, m2, m3));

        BusModelRecommendation result = useCase.execute("R1", 0.80);

        long recommendedCount = result.getRecommendations().getWeekday().getModels()
                .stream().filter(BusModelRank::isRecommended).count();

        assertEquals(1, recommendedCount);
    }

    // ── AC2 — Sorting: eligible by cost ASC, ineligible by capacity DESC ─────

    @Test
    void executeShouldSortEligibleModelsByUnitCostAscending() {
        // Route 20km → minAuto=40km, reqCap=125 (from 5000 demand, 10-min freq, 0.80 occ)
        BusModel cheap  = model(1L, 150, 100, 100_000);
        BusModel mid    = model(2L, 150, 100, 200_000);
        BusModel costly = model(3L, 150, 100, 300_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(costly, cheap, mid));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        assertEquals(1L, models.get(0).getModel().getId()); // cheapest first
        assertEquals(2L, models.get(1).getModel().getId());
        assertEquals(3L, models.get(2).getModel().getId());
    }

    @Test
    void executeShouldSortIneligibleModelsByPassengerCapacityDescending() {
        // Route 20km, freq=10 → reqCap=125. These models all have capacity < 125 → all ineligible
        BusModel small  = model(1L, 50,  100, 100_000);
        BusModel medium = model(2L, 80,  100, 100_000);
        BusModel large  = model(3L, 120, 100, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(small, medium, large));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        // ineligible → sorted by capacity DESC
        assertEquals(3L, models.get(0).getModel().getId()); // 120 pax
        assertEquals(2L, models.get(1).getModel().getId()); // 80 pax
        assertEquals(1L, models.get(2).getModel().getId()); // 50 pax
    }

    @Test
    void executeShouldPlaceEligibleModelsBeforeIneligible() {
        // Route 20km, freq=10 → reqCap=125
        BusModel eligible   = model(1L, 150, 100, 300_000); // capacity ok, autonomy ok
        BusModel ineligible = model(2L,  50, 100, 100_000); // capacity too low (cheapest but ineligible)

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(ineligible, eligible));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        assertTrue(models.get(0).isMeetsCapacity(), "eligible model must appear first");
        assertFalse(models.get(1).isMeetsCapacity(), "ineligible model must appear last");
    }

    // ── AC2 — Rank is 1-based sequential ─────────────────────────────────────

    @Test
    void executeShouldAssign1BasedSequentialRanksToAllModels() {
        BusModel m1 = model(1L, 150, 100, 100_000);
        BusModel m2 = model(2L, 150, 100, 200_000);
        BusModel m3 = model(3L, 150, 100, 300_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m1, m2, m3));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        assertEquals(1, models.get(0).getRank());
        assertEquals(2, models.get(1).getRank());
        assertEquals(3, models.get(2).getRank());
    }

    // ── AC3 — No eligible model ───────────────────────────────────────────────

    @Test
    void executeShouldReturnNoRecommendedWhenNoModelMeetsCapacity() {
        // Route 20km, freq=10 → reqCap=125; all models have capacity < 125
        BusModel m1 = model(1L, 50, 100, 100_000);
        BusModel m2 = model(2L, 80, 100, 200_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m1, m2));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        assertTrue(models.stream().noneMatch(BusModelRank::isRecommended),
                "No model should be recommended when none meets capacity");
    }

    @Test
    void executeShouldSetMeetsCapacityFalseForAllModelsWhenCapacityExceedsAll() {
        BusModel m1 = model(1L, 50, 100, 100_000);
        BusModel m2 = model(2L, 80, 100, 200_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m1, m2));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        assertTrue(models.stream().noneMatch(BusModelRank::isMeetsCapacity));
    }

    // ── AC3 — meetsCapacity / meetsAutonomy flags ─────────────────────────────

    @Test
    void executeShouldSetMeetsCapacityFalseWhenPassengerCapacityIsNull() {
        // Route 20km, freq=10 → reqCap=125; null capacity → not eligible
        BusModel m = modelNullCapacity(1L, 100);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        BusModelRank rank = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0);

        assertFalse(rank.isMeetsCapacity());
        assertFalse(rank.isRecommended());
    }

    @Test
    void executeShouldSetMeetsAutonomyFalseWhenAutonomyKmIsNull() {
        // Route 20km → minAutonomy=40km; autonomyKm=null → not eligible
        BusModel m = modelNullAutonomy(1L, 200);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        BusModelRank rank = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0);

        assertFalse(rank.isMeetsAutonomy());
        assertFalse(rank.isRecommended());
    }

    @Test
    void executeShouldSetMeetsCapacityFalseWhenCapacityBelowRequired() {
        // reqCap=125; model capacity=80 → meetsCapacity=false
        BusModel m = model(1L, 80, 100, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        BusModelRank rank = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0);

        assertFalse(rank.isMeetsCapacity());
    }

    @Test
    void executeShouldSetMeetsAutonomyFalseWhenAutonomyBelowMinimum() {
        // Route 20km → minAutonomy=40km; model autonomy=30km → meetsAutonomy=false
        BusModel m = model(1L, 200, 30, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(1000, 800, 600)); // low demand → small reqCap so capacity isn't issue
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        BusModelRank rank = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0);

        assertFalse(rank.isMeetsAutonomy());
    }

    // ── AC2 — Justification strings ───────────────────────────────────────────

    @Test
    void executeShouldBuildJustificationWhenBothCriteriaMet() {
        // Route 10km → minAutonomy=20km. reqCap=15 (1000 demand, freq=0 fallback, occ=0.80)
        // Model: capacity=100 (≥15), autonomy=100km (≥20km) → both met
        BusModel m = model(1L, 100, 100, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 10.0, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(1000, 800, 600));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        String justification = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0).getJustification();

        assertTrue(justification.contains("Cumple capacidad"),
                "Justification should state both criteria are met, was: " + justification);
        assertTrue(justification.contains("autonomía"),
                "Justification should mention autonomy, was: " + justification);
    }

    @Test
    void executeShouldBuildJustificationWhenCapacityFailsOnly() {
        // Route 10km → minAutonomy=20km, reqCap=15
        // Model: capacity=5 (<15) → fails capacity; autonomy=100km (≥20km) → passes
        BusModel m = model(1L, 5, 100, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 10.0, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(1000, 800, 600));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        String justification = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0).getJustification();

        assertTrue(justification.contains("Capacidad insuficiente"),
                "Justification should state capacity is insufficient, was: " + justification);
        assertFalse(justification.contains("autonomía insuficiente"),
                "Justification should NOT mention insufficient autonomy, was: " + justification);
    }

    @Test
    void executeShouldBuildJustificationWhenAutonomyFailsOnly() {
        // Route 20km → minAutonomy=40km, reqCap=15
        // Model: capacity=100 (≥15) → passes; autonomy=10km (<40km) → fails
        BusModel m = model(1L, 100, 10, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 0)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(1000, 800, 600));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        String justification = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0).getJustification();

        assertTrue(justification.contains("Autonomía insuficiente"),
                "Justification should state autonomy is insufficient, was: " + justification);
        assertFalse(justification.contains("Capacidad insuficiente"),
                "Justification should NOT mention insufficient capacity, was: " + justification);
    }

    @Test
    void executeShouldBuildJustificationWhenBothCriteriaFail() {
        // Route 20km → minAutonomy=40km, reqCap=125
        // Model: capacity=5 (<125) + autonomy=10km (<40km) → both fail
        BusModel m = model(1L, 5, 10, 100_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m));

        String justification = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels().get(0).getJustification();

        assertTrue(justification.contains("Capacidad insuficiente"),
                "Justification should mention capacity failure, was: " + justification);
        assertTrue(justification.contains("autonomía insuficiente"),
                "Justification should mention autonomy failure, was: " + justification);
    }

    // ── AC2 — requiredCapacity on each rank ──────────────────────────────────

    @Test
    void executeShouldSetRequiredCapacityOnEachModelRank() {
        BusModel m1 = model(1L, 150, 100, 100_000);
        BusModel m2 = model(2L,  50, 100, 200_000);

        when(routeRepository.findByIdWithTimeComparison("R1"))
                .thenReturn(Optional.of(route("1", 20.0, 10)));
        when(afluenciaRepository.findAvgDemandByLinea("linea 1"))
                .thenReturn(demand(5000, 3000, 2000));
        when(busModelRepository.findAll()).thenReturn(List.of(m1, m2));

        List<BusModelRank> models = useCase.execute("R1", 0.80)
                .getRecommendations().getWeekday().getModels();

        // Both ranks should report the same requiredCapacity for the day
        assertEquals(models.get(0).getRequiredCapacity(), models.get(1).getRequiredCapacity());
        assertTrue(models.get(0).getRequiredCapacity() > 0);
    }
}
