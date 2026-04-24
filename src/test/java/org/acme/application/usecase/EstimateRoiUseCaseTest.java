package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.RoiEstimate;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstimateRoiUseCaseTest {

    private BusModelRepository busModelRepository;
    private RouteRepository routeRepository;
    private EstimateRoiUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        routeRepository = mock(RouteRepository.class);
        useCase = new EstimateRoiUseCase(busModelRepository, routeRepository);
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
        model.setName("Test Electric");
        model.setManufacturer("Test");
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
        model.setName("Test Diesel");
        model.setManufacturer("Test");
        model.setFuelType(FuelType.DIESEL);
        model.setUnitCostUsd(new BigDecimal("120000"));
        model.setFuelConsumptionLKm(new BigDecimal("0.35"));
        model.setEnergyConsumptionKwhKm(new BigDecimal("0.0"));
        model.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        model.setCo2EmissionsGKm(new BigDecimal("940"));
        return model;
    }

    @Test
    void executeShouldReturnValidRoiWhenInputIsValid() {
        Route route = buildRoute("R1", 20.0);
        BusModel electric = buildElectricModel();
        BusModel diesel = buildDieselModel();

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        RoiEstimate result = useCase.execute("R1", 1L, 10);

        assertTrue(result.getRoiPercent() > 0, "ROI debe ser positivo");
        assertTrue(result.getPaybackYears() > 0, "Payback debe ser positivo");
        assertTrue(result.getNetAnnualReturn() > 0, "Ahorro neto debe ser positivo");
        assertTrue(result.getTotalInvestmentMXN() > 0, "Inversión debe ser positiva");
        assertTrue(result.getCo2AvoidedTons() > 0, "CO2 evitado debe ser positivo");
        assertTrue(result.getDieselCostPerYear() > result.getElectricCostPerYear(),
                "Costo diésel debe ser mayor que eléctrico");
    }

    @Test
    void executeShouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 10));
    }

    @Test
    void executeShouldThrowWhenBusModelNotFound() {
        Route route = buildRoute("R1", 20.0);
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 999L, 10));
    }

    @Test
    void executeShouldThrowWhenModelIsDiesel() {
        Route route = buildRoute("R1", 20.0);
        BusModel diesel = buildDieselModel();

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(10L)).thenReturn(Optional.of(diesel));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 10L, 10));
    }

    @Test
    void executeShouldComputeCorrectValues() {
        Route route = buildRoute("R1", 20.0);
        BusModel electric = buildElectricModel();
        BusModel diesel = buildDieselModel();

        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(route));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        RoiEstimate result = useCase.execute("R1", 1L, 1);

        // kmPerBusPerYear = 20 * 10 * 310 = 62000
        double kmPerYear = 62000.0;
        // totalInvestmentMXN = 1 * 420000 * 17.5 = 7350000
        assertEquals(7_350_000.0, result.getTotalInvestmentMXN(), 0.01);
        // electricCost = 1 * 62000 * 1.0 * 2.8 = 173600
        assertEquals(173_600.0, result.getElectricCostPerYear(), 0.01);
        // dieselCost = 1 * 62000 * 0.35 * 24 = 520800
        assertEquals(520_800.0, result.getDieselCostPerYear(), 0.01);
        // maintenanceSavings = 1 * 62000 * (0.22 - 0.12) = 6200
        // netAnnualReturn = 520800 - 173600 + 6200 = 353400
        assertEquals(353_400.0, result.getNetAnnualReturn(), 0.01);
        // roiPercent = (353400 / 7350000) * 100 = 4.808...
        assertEquals(4.808, result.getRoiPercent(), 0.01);
        // paybackYears = 7350000 / 353400 = 20.8...
        assertEquals(20.80, result.getPaybackYears(), 0.01);
        // co2 = (1 * 62000 * 940) / 1000000 = 58.28
        assertEquals(58.28, result.getCo2AvoidedTons(), 0.01);
    }
}
