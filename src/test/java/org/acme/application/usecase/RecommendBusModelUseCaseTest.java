package org.acme.application.usecase;

import org.acme.application.exception.DemandNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.ModelRecommendation;
import org.acme.domain.models.Route;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.acme.domain.models.DayType.WEEKDAY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendBusModelUseCaseTest {

    private AfluenciaMetrobusRepository afluenciaRepository;
    private FrequencyRepository frequencyRepository;
    private RouteRepository routeRepository;
    private BusModelRepository busModelRepository;
    private RecommendBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        frequencyRepository = mock(FrequencyRepository.class);
        routeRepository = mock(RouteRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        useCase = new RecommendBusModelUseCase(
                afluenciaRepository, frequencyRepository, routeRepository, busModelRepository);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private Route buildRoute(String id, double distanceKm) {
        Route r = new Route();
        r.setRouteId(id);
        r.setAgencyId("MB");
        r.setRouteShortName("1");
        r.setDistanceKm(distanceKm);
        return r;
    }

    private BusModel buildElectricModel(Long id, String name, int capacity, double autonomy, double cost) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName(name);
        m.setManufacturer("TestMfg");
        m.setFuelType(FuelType.ELECTRIC);
        m.setPassengerCapacity(capacity);
        m.setAutonomyKm(BigDecimal.valueOf(autonomy));
        m.setUnitCostUsd(BigDecimal.valueOf(cost));
        return m;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void executeShouldReturnRecommendationWhenValidInput() {
        // demand=5000, headway=300s -> busesPerHour=12, peakHour=600
        // requiredCap = CEIL(600 / (12 * 0.80)) = CEIL(62.5) = 63
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("5000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.of(buildRoute("route-1", 30)));

        BusModel large = buildElectricModel(1L, "LargeBus", 85, 300, 420000);
        BusModel small = buildElectricModel(2L, "SmallBus", 50, 130, 300000);
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of(large, small));

        ModelRecommendation result = useCase.execute("linea 1", "weekday", 80);

        assertEquals(63, result.getRequiredCapacity());
        assertEquals(1, result.getModels().size());
        assertEquals(1L, result.getModels().get(0).getId());
    }

    @Test
    void executeShouldThrowWhenNoDataForLinea() {
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(null);
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.empty());
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of());

        assertThrows(DemandNotFoundException.class,
                () -> useCase.execute("linea 1", "weekday", 80));
    }

    @Test
    void executeShouldReturnEmptyModelsWhenNoneEligible() {
        // demand=100000 -> peakHour=12000, headway=300 -> busesPerHour=12
        // requiredCap = CEIL(12000 / (12 * 0.80)) = CEIL(1250) = 1250 -> all models too small
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("100000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.of(buildRoute("route-1", 30)));

        BusModel small = buildElectricModel(1L, "SmallBus", 50, 300, 200000);
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of(small));

        ModelRecommendation result = useCase.execute("linea 1", "weekday", 80);

        assertTrue(result.getModels().isEmpty());
    }

    @Test
    void executeShouldMarkCheapestEligibleAsRecommended() {
        // demand=5000, headway=300 -> requiredCap=63
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("5000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.of(buildRoute("route-1", 30)));

        BusModel expensive = buildElectricModel(1L, "ExpensiveBus", 90, 300, 600000);
        BusModel cheap = buildElectricModel(2L, "CheapBus", 80, 300, 300000);
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of(expensive, cheap));

        ModelRecommendation result = useCase.execute("linea 1", "weekday", 80);

        assertEquals(2, result.getModels().size());
        ModelRecommendation.ModelCandidate cheapCandidate = result.getModels().stream()
                .filter(c -> c.getId() == 2L)
                .findFirst()
                .orElseThrow();
        assertTrue(cheapCandidate.isRecommended());

        ModelRecommendation.ModelCandidate expensiveCandidate = result.getModels().stream()
                .filter(c -> c.getId() == 1L)
                .findFirst()
                .orElseThrow();
        assertFalse(expensiveCandidate.isRecommended());
    }

    @Test
    void executeShouldThrowWhenNoFrequencyData() {
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("5000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(null);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.empty());
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> useCase.execute("linea 1", "weekday", 80));
    }

    @Test
    void executeShouldFilterByAutonomyRequirement() {
        // routeDistance=100km -> autonomy required >= 200km
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("5000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.of(buildRoute("route-1", 100)));

        BusModel shortRange = buildElectricModel(1L, "ShortRange", 90, 150, 300000);
        BusModel longRange = buildElectricModel(2L, "LongRange", 90, 300, 400000);
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of(shortRange, longRange));

        ModelRecommendation result = useCase.execute("linea 1", "weekday", 80);

        assertEquals(1, result.getModels().size());
        assertEquals(2L, result.getModels().get(0).getId());
    }

    @Test
    void executeShouldUseDefaultOccupancyWhenNull() {
        // null dayType -> WEEKDAY, null occupancy -> 80 (0.80 default)
        // demand=5000, headway=300 -> peakHour=600, busesPerHour=12
        // requiredCap = CEIL(600 / (12 * 0.80)) = CEIL(62.5) = 63
        when(afluenciaRepository.findAverageDailyDemand("linea 1", WEEKDAY))
                .thenReturn(new BigDecimal("5000"));
        when(frequencyRepository.findAverageHeadwayByRouteShortName("1"))
                .thenReturn(300.0);
        when(routeRepository.findByAgencyAndShortName("MB", "1"))
                .thenReturn(Optional.of(buildRoute("route-1", 30)));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC))
                .thenReturn(List.of());

        ModelRecommendation result = useCase.execute("linea 1", null, null);

        assertEquals(63, result.getRequiredCapacity());
    }
}
