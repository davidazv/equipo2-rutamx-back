package org.acme.application.usecase;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.KpiMetrics;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetKpiMetricsUseCaseTest {

    private BusModelRepository busModelRepository;
    private RouteRepository routeRepository;
    private GetKpiMetricsUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        routeRepository = mock(RouteRepository.class);
        useCase = new GetKpiMetricsUseCase(busModelRepository, routeRepository);
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

    private BusModel buildElectricModel(Long id, String name, String costUsd, String energyKwhKm) {
        BusModel model = new BusModel();
        model.setId(id);
        model.setName(name);
        model.setManufacturer("Test");
        model.setFuelType(FuelType.ELECTRIC);
        model.setUnitCostUsd(new BigDecimal(costUsd));
        model.setEnergyConsumptionKwhKm(new BigDecimal(energyKwhKm));
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
    void executeShouldThrowWhenNoDieselModelsExist() {
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(buildElectricModel(1L, "E1", "420000", "1.0")));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> useCase.execute(10));
    }

    @Test
    void executeShouldReturnZerosWhenNoRoutes() {
        when(routeRepository.findAllWithDistance()).thenReturn(Collections.emptyList());
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(buildElectricModel(1L, "E1", "420000", "1.0")));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(buildDieselModel()));

        KpiMetrics result = useCase.execute(10);

        assertEquals(0, result.getRoutesAnalyzed());
        assertEquals(0.0, result.getTotalCo2AvoidedTons(), 0.01);
        assertEquals(0.0, result.getTotalFuelSavingsMXN(), 0.01);
        assertEquals(0.0, result.getTotalInvestmentMXN(), 0.01);
    }

    @Test
    void executeShouldReturnZerosWhenNoElectricModels() {
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(Collections.emptyList());
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(buildDieselModel()));

        KpiMetrics result = useCase.execute(10);

        assertEquals(1, result.getRoutesAnalyzed());
        assertEquals(0.0, result.getTotalCo2AvoidedTons(), 0.01);
        assertEquals(0.0, result.getTotalFuelSavingsMXN(), 0.01);
    }

    @Test
    void executeShouldComputeCorrectMetricsForSingleRouteAndModel() {
        Route route = buildRoute("R1", 20.0);
        BusModel electric = buildElectricModel(1L, "E1", "420000", "1.0");
        BusModel diesel = buildDieselModel();

        when(routeRepository.findAllWithDistance()).thenReturn(List.of(route));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        KpiMetrics result = useCase.execute(1);

        // kmPerYear = 20 * 10 * 310 = 62000
        // electricCost = 1 * 62000 * 1.0 * 2.8 = 173600
        assertEquals(173_600.0, result.getTotalElectricCostMXN(), 0.01);
        // dieselCost = 1 * 62000 * 0.35 * 24 = 520800
        assertEquals(520_800.0, result.getTotalDieselCostMXN(), 0.01);
        // fuelSavings = 520800 - 173600 = 347200
        assertEquals(347_200.0, result.getTotalFuelSavingsMXN(), 0.01);
        // investment = 1 * 420000 * 17.5 = 7350000
        assertEquals(7_350_000.0, result.getTotalInvestmentMXN(), 0.01);
        // co2 = (1 * 62000 * 940) / 1000000 = 58.28
        assertEquals(58.28, result.getTotalCo2AvoidedTons(), 0.01);
        assertEquals(1, result.getRoutesAnalyzed());
    }

    @Test
    void executeShouldAggregateAcrossMultipleRoutes() {
        Route r1 = buildRoute("R1", 20.0);
        Route r2 = buildRoute("R2", 10.0);
        BusModel electric = buildElectricModel(1L, "E1", "420000", "1.0");
        BusModel diesel = buildDieselModel();

        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r1, r2));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        KpiMetrics result = useCase.execute(1);

        // R1: co2 = (62000*940)/1e6 = 58.28, R2: co2 = (31000*940)/1e6 = 29.14
        assertEquals(87.42, result.getTotalCo2AvoidedTons(), 0.01);
        assertEquals(2, result.getRoutesAnalyzed());
    }

    @Test
    void executeShouldAggregateAcrossMultipleElectricModels() {
        Route route = buildRoute("R1", 20.0);
        BusModel e1 = buildElectricModel(1L, "E1", "420000", "1.0");
        BusModel e2 = buildElectricModel(2L, "E2", "300000", "1.0");
        BusModel diesel = buildDieselModel();

        when(routeRepository.findAllWithDistance()).thenReturn(List.of(route));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(e1, e2));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        KpiMetrics result = useCase.execute(1);

        // investment = (1*420000*17.5) + (1*300000*17.5) = 7350000 + 5250000 = 12600000
        assertEquals(12_600_000.0, result.getTotalInvestmentMXN(), 0.01);
        // co2 per model iteration: 58.28 * 2 = 116.56
        assertEquals(116.56, result.getTotalCo2AvoidedTons(), 0.01);
    }

    @Test
    void executeShouldScaleWithBusesPerRoute() {
        Route route = buildRoute("R1", 20.0);
        BusModel electric = buildElectricModel(1L, "E1", "420000", "1.0");
        BusModel diesel = buildDieselModel();

        when(routeRepository.findAllWithDistance()).thenReturn(List.of(route));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        KpiMetrics with1 = useCase.execute(1);
        KpiMetrics with5 = useCase.execute(5);

        assertEquals(with1.getTotalCo2AvoidedTons() * 5, with5.getTotalCo2AvoidedTons(), 0.01);
        assertEquals(with1.getTotalInvestmentMXN() * 5, with5.getTotalInvestmentMXN(), 0.01);
        assertEquals(with1.getTotalFuelSavingsMXN() * 5, with5.getTotalFuelSavingsMXN(), 0.01);
    }

    @Test
    void executeShouldSetRoutesAnalyzedCount() {
        Route r1 = buildRoute("R1", 20.0);
        Route r2 = buildRoute("R2", 10.0);
        Route r3 = buildRoute("R3", 15.0);
        BusModel electric = buildElectricModel(1L, "E1", "420000", "1.0");
        BusModel diesel = buildDieselModel();

        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r1, r2, r3));
        when(busModelRepository.findByFuelType(FuelType.ELECTRIC)).thenReturn(List.of(electric));
        when(busModelRepository.findByFuelType(FuelType.DIESEL)).thenReturn(List.of(diesel));

        KpiMetrics result = useCase.execute(1);

        assertEquals(3, result.getRoutesAnalyzed());
    }
}
