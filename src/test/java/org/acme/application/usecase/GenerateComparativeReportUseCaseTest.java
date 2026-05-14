package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.ComparativeReport;
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

class GenerateComparativeReportUseCaseTest {

    private RouteRepository routeRepository;
    private BusModelRepository busModelRepository;
    private GenerateComparativeReportUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        useCase = new GenerateComparativeReportUseCase(routeRepository, busModelRepository);
    }

    private Route buildRoute(String id, double distanceKm) {
        Route route = new Route();
        route.setRouteId(id);
        route.setAgencyId("TEST");
        route.setRouteShortName("T1");
        route.setRouteLongName("Test Route");
        route.setRouteType(3);
        route.setDistanceKm(distanceKm);
        return route;
    }

    private BusModel buildElectricModel() {
        BusModel m = new BusModel();
        m.setId(1L);
        m.setName("Electric Bus");
        m.setFuelType(FuelType.ELECTRIC);
        m.setEnergyConsumptionKwhKm(new BigDecimal("1.0"));
        m.setFuelConsumptionLKm(BigDecimal.ZERO);
        m.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        m.setCo2EmissionsGKm(BigDecimal.ZERO);
        m.setUnitCostUsd(new BigDecimal("420000"));
        return m;
    }

    private BusModel buildDieselModel() {
        BusModel m = new BusModel();
        m.setId(4L);
        m.setName("Diesel Bus");
        m.setFuelType(FuelType.DIESEL);
        m.setFuelConsumptionLKm(new BigDecimal("0.35"));
        m.setEnergyConsumptionKwhKm(BigDecimal.ZERO);
        m.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        m.setCo2EmissionsGKm(new BigDecimal("940"));
        m.setUnitCostUsd(new BigDecimal("120000"));
        return m;
    }

    @Test
    void shouldReturnReportWithAllFields() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 10);

        assertEquals("R1", report.getRouteId());
        assertEquals(20.0, report.getRouteDistanceKm());
        assertEquals(10, report.getNumberOfBuses());
        assertEquals("Electric Bus", report.getElectricModelName());
        assertEquals("Diesel Bus", report.getDieselModelName());
    }

    @Test
    void shouldComputeElectricCostCorrectly() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 1);

        // kmPerYear = 20 * 10 * 310 = 62000
        // electricEnergyCost = 1 * 62000 * 1.0 * 2.8 = 173600
        assertEquals(173_600.0, report.getElectricCostPerYear(), 0.01);
        // electricMaintCost = 1 * 62000 * 0.12 = 7440
        assertEquals(7_440.0, report.getElectricMaintenanceCostPerYear(), 0.01);
        // electricTotal = 173600 + 7440 = 181040
        assertEquals(181_040.0, report.getElectricTotalCostPerYear(), 0.01);
    }

    @Test
    void shouldComputeDieselCostCorrectly() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 1);

        // dieselFuelCost = 1 * 62000 * 0.35 * 24 = 520800
        assertEquals(520_800.0, report.getDieselCostPerYear(), 0.01);
        // dieselMaintCost = 1 * 62000 * 0.22 = 13640
        assertEquals(13_640.0, report.getDieselMaintenanceCostPerYear(), 0.01);
        // dieselTotal = 520800 + 13640 = 534440
        assertEquals(534_440.0, report.getDieselTotalCostPerYear(), 0.01);
    }

    @Test
    void shouldComputeSavingsCorrectly() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 1);

        // annualSavings = 534440 - 181040 = 353400
        assertEquals(353_400.0, report.getAnnualSavingsMXN(), 0.01);
        // savingsPercent = (353400 / 534440) * 100 = 66.12...
        assertEquals(66.12, report.getSavingsPercent(), 0.1);
    }

    @Test
    void shouldComputeCo2AvoidedCorrectly() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 1);

        // dieselCo2 = (1 * 62000 * 940) / 1000000 = 58.28
        assertEquals(58.28, report.getDieselCo2TonsPerYear(), 0.01);
        // electricCo2 = 0
        assertEquals(0.0, report.getElectricCo2TonsPerYear(), 0.001);
        assertEquals(58.28, report.getCo2AvoidedTonsPerYear(), 0.01);
    }

    @Test
    void shouldScaleCostsWithNumberOfBuses() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report1 = useCase.execute("R1", 1L, 4L, 1);
        ComparativeReport report10 = useCase.execute("R1", 1L, 4L, 10);

        assertEquals(report1.getElectricTotalCostPerYear() * 10,
                report10.getElectricTotalCostPerYear(), 1.0);
        assertEquals(report1.getDieselTotalCostPerYear() * 10,
                report10.getDieselTotalCostPerYear(), 1.0);
    }

    @Test
    void shouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 4L, 10));
    }

    @Test
    void shouldThrowWhenElectricModelNotFound() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 999L, 4L, 10));
    }

    @Test
    void shouldThrowWhenDieselModelNotFound() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 1L, 999L, 10));
    }

    @Test
    void shouldThrowWhenElectricModelIsActuallyDiesel() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 4L, 4L, 10));
    }

    @Test
    void shouldThrowWhenDieselModelIsActuallyElectric() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 1L, 1L, 10));
    }

    @Test
    void electricTotalShouldBeLowerThanDiesel() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 10);

        assertTrue(report.getElectricTotalCostPerYear() < report.getDieselTotalCostPerYear(),
                "El costo total eléctrico debe ser menor al diésel");
        assertTrue(report.getAnnualSavingsMXN() > 0, "El ahorro anual debe ser positivo");
    }
}
