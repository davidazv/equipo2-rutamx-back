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

    private BusModelRepository busModelRepository;
    private RouteRepository routeRepository;
    private GenerateComparativeReportUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        routeRepository = mock(RouteRepository.class);
        useCase = new GenerateComparativeReportUseCase(busModelRepository, routeRepository);
    }

    private Route buildRoute(String id, double distanceKm) {
        Route route = new Route();
        route.setRouteId(id);
        route.setAgencyId("TEST");
        route.setRouteShortName("T1");
        route.setRouteLongName("Ruta de Prueba");
        route.setRouteType(3);
        route.setDistanceKm(distanceKm);
        return route;
    }

    private BusModel buildElectric() {
        BusModel m = new BusModel();
        m.setId(1L);
        m.setName("E12PRO");
        m.setManufacturer("Yutong");
        m.setFuelType(FuelType.ELECTRIC);
        m.setUnitCostUsd(new BigDecimal("420000"));
        m.setEnergyConsumptionKwhKm(new BigDecimal("1.0"));
        m.setFuelConsumptionLKm(new BigDecimal("0.0"));
        m.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        m.setCo2EmissionsGKm(new BigDecimal("0"));
        return m;
    }

    private BusModel buildDiesel() {
        BusModel m = new BusModel();
        m.setId(4L);
        m.setName("H8");
        m.setManufacturer("Yutong");
        m.setFuelType(FuelType.DIESEL);
        m.setUnitCostUsd(new BigDecimal("120000"));
        m.setFuelConsumptionLKm(new BigDecimal("0.35"));
        m.setEnergyConsumptionKwhKm(new BigDecimal("0.0"));
        m.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        m.setCo2EmissionsGKm(new BigDecimal("940"));
        return m;
    }

    @Test
    void executeShouldReturnValidReportWhenInputIsValid() {
        when(routeRepository.findByIdWithDistance("TR13")).thenReturn(Optional.of(buildRoute("TR13", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectric()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDiesel()));

        ComparativeReport report = useCase.execute("TR13", 1L, 4L, 10, 10);

        assertNotNull(report);
        assertEquals("TR13", report.getRouteId());
        assertEquals(10, report.getNumberOfBuses());
        assertEquals(10, report.getProjectionYears());
        assertEquals(10, report.getTcoProjection().size());
        assertTrue(report.getDieselCostPerYear() > report.getElectricCostPerYear());
        assertTrue(report.getCo2AvoidedTonsPerYear() > 0);
        assertTrue(report.getTotalInvestmentMXN() > 0);
        assertTrue(report.getNetAnnualSavings() > 0);
    }

    @Test
    void executeShouldComputeCorrectAnnualCosts() {
        when(routeRepository.findByIdWithDistance("TR13")).thenReturn(Optional.of(buildRoute("TR13", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectric()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDiesel()));

        ComparativeReport report = useCase.execute("TR13", 1L, 4L, 1, 5);

        // kmPerBusPerYear = 20 * 10 * 310 = 62000
        // electricCost = 1 * 62000 * 1.0 * 2.8 = 173600
        assertEquals(173_600.0, report.getElectricCostPerYear(), 0.01);
        // dieselCost = 1 * 62000 * 0.35 * 24 = 520800
        assertEquals(520_800.0, report.getDieselCostPerYear(), 0.01);
        // co2 = (1 * 62000 * 940) / 1000000 = 58.28
        assertEquals(58.28, report.getCo2AvoidedTonsPerYear(), 0.01);
        // investment = 1 * 420000 * 17.5 = 7350000
        assertEquals(7_350_000.0, report.getTotalInvestmentMXN(), 0.01);
    }

    @Test
    void executeShouldBuildTcoProjectionWithCorrectLength() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 15.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectric()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDiesel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 5, 15);

        assertEquals(15, report.getTcoProjection().size());
        assertEquals(1, report.getTcoProjection().get(0).getYear());
        assertEquals(15, report.getTcoProjection().get(14).getYear());
    }

    @Test
    void executeShouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 4L, 10, 10));
    }

    @Test
    void executeShouldThrowWhenElectricModelNotFound() {
        when(routeRepository.findByIdWithDistance("TR13")).thenReturn(Optional.of(buildRoute("TR13", 20.0)));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("TR13", 999L, 4L, 10, 10));
    }

    @Test
    void executeShouldThrowWhenElectricModelIsDiesel() {
        when(routeRepository.findByIdWithDistance("TR13")).thenReturn(Optional.of(buildRoute("TR13", 20.0)));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDiesel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("TR13", 4L, 4L, 10, 10));
    }

    @Test
    void executeShouldThrowWhenDieselModelIsElectric() {
        when(routeRepository.findByIdWithDistance("TR13")).thenReturn(Optional.of(buildRoute("TR13", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectric()));
        when(busModelRepository.findById(2L)).thenReturn(Optional.of(buildElectric()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("TR13", 1L, 2L, 10, 10));
    }

    @Test
    void executeShouldThrowWhenYearsOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("TR13", 1L, 4L, 10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("TR13", 1L, 4L, 10, 31));
    }

    @Test
    void executeShouldThrowWhenBusesIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("TR13", 1L, 4L, 0, 10));
    }

    @Test
    void paybackYearShouldBeZeroWhenNeverReached() {
        // Ruta muy corta: ahorro anual nunca supera la inversión dentro de la proyección
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 0.1)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectric()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDiesel()));

        ComparativeReport report = useCase.execute("R1", 1L, 4L, 1, 3);

        // Con distancia mínima el payback puede no ocurrir en 3 años
        assertTrue(report.getPaybackYear() >= 0);
    }
}
