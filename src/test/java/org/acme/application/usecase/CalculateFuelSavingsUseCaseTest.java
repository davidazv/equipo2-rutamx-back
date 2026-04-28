package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelSavings;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculateFuelSavingsUseCaseTest {

    private BusModelRepository busModelRepository;
    private RouteRepository routeRepository;
    private CalculateFuelSavingsUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        routeRepository = mock(RouteRepository.class);
        useCase = new CalculateFuelSavingsUseCase(busModelRepository, routeRepository);
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

    private BusModel buildElectricModel() {
        BusModel model = new BusModel();
        model.setId(1L);
        model.setName("E12PRO");
        model.setManufacturer("Yutong");
        model.setFuelType(FuelType.ELECTRIC);
        model.setUnitCostUsd(new BigDecimal("420000"));
        model.setEnergyConsumptionKwhKm(new BigDecimal("1.0"));
        model.setFuelConsumptionLKm(new BigDecimal("0.0"));
        model.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        model.setCo2EmissionsGKm(new BigDecimal("0"));
        return model;
    }

    private BusModel buildDieselModel() {
        BusModel model = new BusModel();
        model.setId(10L);
        model.setName("ZK6128HGD");
        model.setManufacturer("Yutong");
        model.setFuelType(FuelType.DIESEL);
        model.setUnitCostUsd(new BigDecimal("120000"));
        model.setFuelConsumptionLKm(new BigDecimal("0.35"));
        model.setEnergyConsumptionKwhKm(new BigDecimal("0.0"));
        model.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        model.setCo2EmissionsGKm(new BigDecimal("940"));
        return model;
    }

    private void stubValidScenario(String routeId, double distanceKm) {
        when(routeRepository.findByIdWithDistance(routeId)).thenReturn(Optional.of(buildRoute(routeId, distanceKm)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(buildDieselModel()));
    }

    @Test
    void executeShouldReturnValidSavingsWhenInputIsValid() {
        stubValidScenario("R1", 20.0);

        FuelSavings result = useCase.execute("R1", 1L, 10, 5);

        assertTrue(result.getFuelSavingsMXN() > 0, "Savings MXN must be positive");
        assertTrue(result.getFuelSavingsLiters() > 0, "Savings liters must be positive");
        assertEquals("R1", result.getRouteId());
        assertEquals(1L, result.getBusModelId());
        assertEquals(10, result.getNumberOfBuses());
        assertEquals(5, result.getProjectionYears());
        assertEquals("Yutong E12PRO", result.getBusModelName());
    }

    @Test
    void executeShouldComputeCorrectValues() {
        stubValidScenario("R1", 20.0);

        FuelSavings result = useCase.execute("R1", 1L, 1, 5);

        // kmPerYear = 20 * 10 * 310 = 62000
        // dieselCostPerYear = 1 * 62000 * 0.35 * 24.0 = 520800
        assertEquals(520_800.0, result.getDieselCostPerYear(), 0.01);
        // electricCostPerYear = 1 * 62000 * 1.0 * 2.8 = 173600
        assertEquals(173_600.0, result.getElectricCostPerYear(), 0.01);
        // fuelSavingsMXN = 520800 - 173600 = 347200
        assertEquals(347_200.0, result.getFuelSavingsMXN(), 0.01);
        // fuelSavingsLiters = 1 * 62000 * 0.35 = 21700
        assertEquals(21_700.0, result.getFuelSavingsLiters(), 0.01);
        // metadata
        assertEquals(20.0, result.getRouteDistanceKm(), 0.01);
        assertEquals(24.0, result.getDieselReferencePriceMXN(), 0.01);
        assertEquals(0.35, result.getDieselConsumptionLKm(), 0.001);
    }

    @Test
    void executeShouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 10, 5));
    }

    @Test
    void executeShouldThrowWhenBusModelNotFound() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 999L, 10, 5));
    }

    @Test
    void executeShouldThrowWhenModelIsDiesel() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(10L)).thenReturn(Optional.of(buildDieselModel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 10L, 10, 5));
    }

    @Test
    void executeShouldThrowWhenNoDieselBaseline() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class,
                () -> useCase.execute("R1", 1L, 10, 5));
    }

    @Test
    void executeShouldIncludeDieselReferencePrice() {
        stubValidScenario("R1", 20.0);

        FuelSavings result = useCase.execute("R1", 1L, 10, 5);

        assertEquals(24.0, result.getDieselReferencePriceMXN(), 0.001);
        assertEquals(0.35, result.getDieselConsumptionLKm(), 0.001);
    }

    @Test
    void executeShouldHandleSingleBus() {
        stubValidScenario("R1", 20.0);

        FuelSavings result = useCase.execute("R1", 1L, 1, 1);

        assertEquals(1, result.getNumberOfBuses());
        assertEquals(1, result.getProjectionYears());
        assertTrue(result.getFuelSavingsMXN() > 0);
        assertTrue(result.getFuelSavingsLiters() > 0);
    }

    @Test
    void executeShouldHandleLargeFleet() {
        stubValidScenario("R1", 20.0);

        FuelSavings result = useCase.execute("R1", 1L, 200, 5);

        assertEquals(200, result.getNumberOfBuses());
        // 200 buses should produce 200x the savings of 1 bus
        FuelSavings single = useCase.execute("R1", 1L, 1, 5);
        assertEquals(single.getFuelSavingsMXN() * 200, result.getFuelSavingsMXN(), 0.01);
        assertEquals(single.getFuelSavingsLiters() * 200, result.getFuelSavingsLiters(), 0.01);
    }
}
