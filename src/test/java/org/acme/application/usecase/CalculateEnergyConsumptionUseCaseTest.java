package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.EnergyConsumption;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculateEnergyConsumptionUseCaseTest {

    private RouteRepository routeRepository;
    private BusModelRepository busModelRepository;
    private CalculateEnergyConsumptionUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        useCase = new CalculateEnergyConsumptionUseCase(routeRepository, busModelRepository);
    }

    private Route buildRoute(String id, double distanceKm) {
        Route route = new Route();
        route.setRouteId(id);
        route.setAgencyId("TEST_AGENCY");
        route.setRouteShortName("T1");
        route.setRouteLongName("Test Route");
        route.setRouteType(3);
        route.setDistanceKm(distanceKm);
        return route;
    }

    private BusModel buildElectricModel(Long id, double consumptionKwhKm, double batteryKwh, int passengers) {
        BusModel model = new BusModel();
        model.setId(id);
        model.setName("Test Electric");
        model.setManufacturer("Test");
        model.setFuelType(FuelType.ELECTRIC);
        model.setAutonomyKm(new BigDecimal("300"));
        model.setPassengerCapacity(passengers);
        model.setUnitCostUsd(new BigDecimal("420000"));
        model.setBatteryCapacityKwh(new BigDecimal(String.valueOf(batteryKwh)));
        model.setEnergyConsumptionKwhKm(new BigDecimal(String.valueOf(consumptionKwhKm)));
        model.setFuelConsumptionLKm(new BigDecimal("0.0"));
        model.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        model.setCo2EmissionsGKm(new BigDecimal("0"));
        return model;
    }

    private BusModel buildDieselModel() {
        BusModel model = new BusModel();
        model.setId(10L);
        model.setName("Test Diesel");
        model.setManufacturer("Test");
        model.setFuelType(FuelType.DIESEL);
        model.setFuelConsumptionLKm(new BigDecimal("0.35"));
        model.setEnergyConsumptionKwhKm(new BigDecimal("0.0"));
        model.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        model.setCo2EmissionsGKm(new BigDecimal("940"));
        return model;
    }

    @Test
    void executeShouldReturnConsumptionWhenValidInputs() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        assertEquals("R1", result.getRouteId());
        assertEquals(20.0, result.getRouteDistanceKm(), 0.01);
        assertEquals(1L, result.getBusModelId());
        assertEquals("Test Electric", result.getBusModelName());
        assertEquals(50, result.getOccupancyPercent());
        assertTrue(result.getEstimatedConsumptionKwh() > 0);
        assertTrue(result.getBatteryPercentAfter() >= 0);
        assertTrue(result.getRemainingRangeKm() >= 0);
    }

    @Test
    void executeShouldComputeCorrectValuesAt50Percent() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        // occupancyFactor = 1 + (50/100) * 0.003 * 80 = 1.12
        // totalFactor = 1.12 * 1.15 * 1.1 = 1.4168
        // consumption = 20 * 1.0 * 1.4168 = 28.336 → 28.3
        assertEquals(28.3, result.getEstimatedConsumptionKwh(), 0.1);
        // batteryUsed% = (28.336 / 300) * 100 = 9.445
        // batteryAfter = 100 - 9.445 = 90.555 → 90.6
        assertEquals(90.6, result.getBatteryPercentAfter(), 0.1);
        // remainingRange = (300 - 28.336) / (1.0 * 1.4168) = 191.7
        assertEquals(191.7, result.getRemainingRangeKm(), 0.5);
        assertTrue(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldComputeCorrectValuesAt0Percent() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 0);

        // occupancyFactor = 1.0 (no passengers)
        // totalFactor = 1.0 * 1.15 * 1.1 = 1.265
        // consumption = 20 * 1.0 * 1.265 = 25.3
        assertEquals(25.3, result.getEstimatedConsumptionKwh(), 0.01);
        assertTrue(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldReturnCannotCompleteWhenBatteryInsufficient() {
        Route route = buildRoute("R1", 100.0);
        BusModel bus = buildElectricModel(1L, 1.0, 50, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        // consumption = 100 * 1.0 * 1.4168 = 141.68 > 50 kWh battery
        assertEquals(0.0, result.getBatteryPercentAfter(), 0.01);
        assertEquals(0.0, result.getRemainingRangeKm(), 0.01);
        assertFalse(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 50));
    }

    @Test
    void executeShouldThrowWhenBusModelNotFound() {
        Route route = buildRoute("R1", 20.0);
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 999L, 50));
    }

    @Test
    void executeShouldThrowWhenModelNotElectric() {
        Route route = buildRoute("R1", 20.0);
        BusModel diesel = buildDieselModel();

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(10L)).thenReturn(Optional.of(diesel));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 10L, 50));
    }

    @Test
    void executeShouldComputeCorrectValuesAt100Percent() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 100);

        // occupancyFactor = 1 + (100/100) * 0.003 * 80 = 1.24
        // totalFactor = 1.24 * 1.15 * 1.1 = 1.5686
        // consumption = 20 * 1.0 * 1.5686 = 31.372 → 31.4
        assertEquals(31.4, result.getEstimatedConsumptionKwh(), 0.1);
        assertTrue(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldReturnCanCompleteWhenBatteryJustAboveThreshold() {
        // Engineer a scenario where battery lands just above 10%
        // Need batteryPercentAfter > 10.0
        // With 20km, 1.0 kWh/km, 80 passengers, 50% occupancy:
        //   totalFactor = 1.4168, consumption = 20 * 1.4168 = 28.336
        //   batteryUsed% = (28.336 / batteryCapacity) * 100
        //   We want 100 - batteryUsed% ≈ 10.5 → batteryUsed% = 89.5
        //   batteryCapacity = 28.336 / 0.895 ≈ 31.66
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 31.66, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        assertTrue(result.getBatteryPercentAfter() > 10.0);
        assertTrue(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldReturnCannotCompleteWhenBatteryExactlyAtThreshold() {
        // canComplete uses > (strict), so exactly 10% should be false
        // batteryUsed% = 90.0 → batteryAfter = 10.0
        // consumption / capacity = 0.9 → capacity = consumption / 0.9
        // consumption = 20 * 1.0 * 1.265 = 25.3 (at 0% occupancy for simpler math)
        // capacity = 25.3 / 0.9 = 28.111...
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 28.111, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 0);

        // batteryAfter rounds to 10.0 → canComplete = (10.0 > 10.0) = false
        assertEquals(10.0, result.getBatteryPercentAfter(), 0.1);
        assertFalse(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldHandleHighCapacityBus() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.3, 155.33, 140);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        // occupancyFactor = 1 + (50/100) * 0.003 * 140 = 1.21
        // totalFactor = 1.21 * 1.15 * 1.1 = 1.53065
        // consumption = 20 * 1.3 * 1.53065 = 39.797 → 39.8
        assertEquals(39.8, result.getEstimatedConsumptionKwh(), 0.1);
        assertTrue(result.getEstimatedConsumptionKwh() > 0);
    }

    @Test
    void executeShouldHandleLongRoute() {
        Route route = buildRoute("R1", 200.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption result = useCase.execute("R1", 1L, 50);

        // consumption = 200 * 1.0 * 1.4168 = 283.36 → 283.4
        assertEquals(283.4, result.getEstimatedConsumptionKwh(), 0.1);
        // batteryUsed% = (283.36 / 300) * 100 = 94.45 → batteryAfter = 5.5
        assertTrue(result.getBatteryPercentAfter() < 10.0);
        assertFalse(result.isCanCompleteRoute());
    }

    @Test
    void executeShouldIncreaseConsumptionWithHigherOccupancy() {
        Route route = buildRoute("R1", 20.0);
        BusModel bus = buildElectricModel(1L, 1.0, 300, 80);

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(bus));

        EnergyConsumption empty = useCase.execute("R1", 1L, 0);
        EnergyConsumption half = useCase.execute("R1", 1L, 50);
        EnergyConsumption full = useCase.execute("R1", 1L, 100);

        assertTrue(empty.getEstimatedConsumptionKwh() < half.getEstimatedConsumptionKwh());
        assertTrue(half.getEstimatedConsumptionKwh() < full.getEstimatedConsumptionKwh());
    }
}
