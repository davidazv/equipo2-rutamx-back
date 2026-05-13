package org.acme.application.usecase;

import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.CostBenefitReport;
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

class GenerateCostBenefitReportUseCaseTest {

    private RouteRepository routeRepository;
    private BusModelRepository busModelRepository;
    private GenerateCostBenefitReportUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        useCase = new GenerateCostBenefitReportUseCase(routeRepository, busModelRepository);
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
    void shouldReturnCorrectNumberOfPoints() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 10, 10);

        assertEquals(10, report.getPoints().size());
    }

    @Test
    void shouldReturnMetadata() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 10, 10);

        assertEquals("R1", report.getRouteId());
        assertEquals(20.0, report.getRouteDistanceKm());
        assertEquals(10, report.getNumberOfBuses());
        assertEquals("Electric Bus", report.getElectricModelName());
        assertEquals("Diesel Bus", report.getDieselModelName());
        assertTrue(report.getTotalInvestmentMXN() > 0);
    }

    @Test
    void electricCumulativeShouldStartHigherDueToBInvestment() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 1, 10);

        CostBenefitReport.CostBenefitPoint year1 = report.getPoints().get(0);
        assertTrue(year1.getElectricCumulativeMXN() > year1.getDieselCumulativeMXN(),
                "Año 1: eléctrico debe ser más caro por la inversión inicial");
    }

    @Test
    void electricShouldEventuallyBeChEapest() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 1, 30);

        CostBenefitReport.CostBenefitPoint lastPoint = report.getPoints().get(report.getPoints().size() - 1);
        assertTrue(lastPoint.getElectricCumulativeMXN() < lastPoint.getDieselCumulativeMXN(),
                "Año 30: eléctrico debe ser más barato que diésel");
    }

    @Test
    void shouldMarkExactlyOneBreakEvenPoint() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 1, 30);

        long breakEvenCount = report.getPoints().stream()
                .filter(CostBenefitReport.CostBenefitPoint::isBreakEvenYear)
                .count();
        assertEquals(1, breakEvenCount, "Debe haber exactamente un punto de equilibrio");
    }

    @Test
    void cumulativeCostsShouldIncreaseEachYear() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 1, 10);

        for (int i = 1; i < report.getPoints().size(); i++) {
            assertTrue(report.getPoints().get(i).getElectricCumulativeMXN() >
                            report.getPoints().get(i - 1).getElectricCumulativeMXN(),
                    "El costo acumulado eléctrico debe crecer cada año");
            assertTrue(report.getPoints().get(i).getDieselCumulativeMXN() >
                            report.getPoints().get(i - 1).getDieselCumulativeMXN(),
                    "El costo acumulado diésel debe crecer cada año");
        }
    }

    @Test
    void paybackYearsShouldBePositive() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        CostBenefitReport report = useCase.execute("R1", 1L, 4L, 1, 10);

        assertTrue(report.getPaybackYears() > 0);
    }

    @Test
    void shouldThrowWhenRouteNotFound() {
        when(routeRepository.findByIdWithDistance("INVALID")).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> useCase.execute("INVALID", 1L, 4L, 10, 10));
    }

    @Test
    void shouldThrowWhenElectricModelNotFound() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 999L, 4L, 10, 10));
    }

    @Test
    void shouldThrowWhenDieselModelNotFound() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusModelNotFoundException.class,
                () -> useCase.execute("R1", 1L, 999L, 10, 10));
    }

    @Test
    void shouldThrowWhenElectricModelIsActuallyDiesel() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(4L)).thenReturn(Optional.of(buildDieselModel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 4L, 4L, 10, 10));
    }

    @Test
    void shouldThrowWhenDieselModelIsActuallyElectric() {
        when(routeRepository.findByIdWithDistance("R1")).thenReturn(Optional.of(buildRoute("R1", 20.0)));
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("R1", 1L, 1L, 10, 10));
    }
}
