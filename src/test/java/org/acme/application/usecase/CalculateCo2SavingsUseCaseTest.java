package org.acme.application.usecase;

import org.acme.application.EmissionConstants;
import org.acme.application.FleetConstants;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.NoGtfsDataException;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.Co2SavingsResult;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.models.RouteTripsPerDay;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculateCo2SavingsUseCaseTest {

    private RouteRepository routeRepository;
    private BusModelRepository busModelRepository;
    private TripRepository tripRepository;
    private AfluenciaMetrobusRepository afluenciaRepository;
    private CalculateCo2SavingsUseCase useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        busModelRepository = mock(BusModelRepository.class);
        tripRepository = mock(TripRepository.class);
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        useCase = new CalculateCo2SavingsUseCase(routeRepository, busModelRepository,
                tripRepository, afluenciaRepository);
    }

    private BusModel buildElectricModel(Long id, double kwhPerKm) {
        BusModel m = new BusModel();
        m.setId(id);
        m.setName("Test Electric");
        m.setManufacturer("Test");
        m.setFuelType(FuelType.ELECTRIC);
        m.setAutonomyKm(new BigDecimal("300"));
        m.setPassengerCapacity(85);
        m.setUnitCostUsd(new BigDecimal("420000"));
        m.setBatteryCapacityKwh(new BigDecimal("352"));
        m.setEnergyConsumptionKwhKm(new BigDecimal(String.valueOf(kwhPerKm)));
        m.setFuelConsumptionLKm(new BigDecimal("0.0"));
        m.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        m.setCo2EmissionsGKm(new BigDecimal("0"));
        return m;
    }

    private BusModel buildDieselModel() {
        BusModel m = new BusModel();
        m.setId(10L);
        m.setName("Diesel");
        m.setManufacturer("Test");
        m.setFuelType(FuelType.DIESEL);
        m.setEnergyConsumptionKwhKm(new BigDecimal("0.0"));
        m.setFuelConsumptionLKm(new BigDecimal("0.35"));
        m.setMaintenanceCostPerKm(new BigDecimal("0.22"));
        m.setCo2EmissionsGKm(new BigDecimal("940"));
        return m;
    }

    private Route buildRoute(String id, String shortName, String longName, String agencyId, double distKm) {
        Route r = new Route();
        r.setRouteId(id);
        r.setAgencyId(agencyId);
        r.setRouteShortName(shortName);
        r.setRouteLongName(longName);
        r.setRouteType(3);
        r.setDistanceKm(distKm);
        return r;
    }

    private RouteTripsPerDay buildTripsPerDay(String routeId, String shortName, String agencyColor, int tripsPerDay) {
        RouteTripsPerDay t = new RouteTripsPerDay();
        t.setRouteId(routeId);
        t.setRouteShortName(shortName);
        t.setRouteLongName("Route " + routeId);
        t.setAgencyId("AGENCY");
        t.setAgencyColor(agencyColor);
        t.setMonday(tripsPerDay);
        t.setTuesday(tripsPerDay);
        t.setWednesday(tripsPerDay);
        t.setThursday(tripsPerDay);
        t.setFriday(tripsPerDay);
        t.setSaturday(tripsPerDay);
        t.setSunday(tripsPerDay);
        return t;
    }

    @Test
    void executeShouldThrowWhenBusModelNotFound() {
        when(busModelRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusModelNotFoundException.class, () -> useCase.execute(999L));
    }

    @Test
    void executeShouldThrowWhenModelNotElectric() {
        when(busModelRepository.findById(10L)).thenReturn(Optional.of(buildDieselModel()));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(10L));
    }

    @Test
    void executeShouldThrowNoGtfsDataWhenNoRoutes() {
        when(busModelRepository.findById(1L)).thenReturn(Optional.of(buildElectricModel(1L, 1.0)));
        when(routeRepository.findAllWithDistance()).thenReturn(Collections.emptyList());
        assertThrows(NoGtfsDataException.class, () -> useCase.execute(1L));
    }

    @Test
    void executeShouldReturnResultsForEachRoute() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r1 = buildRoute("R1", "1", "Route 1", "AG1", 20.0);
        Route r2 = buildRoute("R2", "2", "Route 2", "AG1", 15.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r1, r2));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "FF0000", 5),
                                    buildTripsPerDay("R2", "2", "00FF00", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);
        assertEquals(2, results.size());
    }

    @Test
    void executeShouldComputeCorrectEmissionsForRoute() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r1 = buildRoute("R1", "1", "Route 1", "AG1", 20.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r1));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "FF0000", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);
        Co2SavingsResult result = results.get(0);

        // annualKm = 20 * 10 * 310 = 62000
        // dieselTon = 62000 * 0.30 * 2.68 / 1000 = 49.848
        double expectedDiesel = 20.0 * (EmissionConstants.DIESEL_LITERS_PER_100KM / 100.0)
                * EmissionConstants.DIESEL_CO2_KG_PER_LITER
                * FleetConstants.DAILY_TRIPS * FleetConstants.OPERATING_DAYS / 1000.0;
        // electricTon = 62000 * 1.0 * 0.454 / 1000 = 28.148
        double expectedElectric = 20.0 * 1.0
                * EmissionConstants.ELECTRIC_CO2_KG_PER_KWH
                * FleetConstants.DAILY_TRIPS * FleetConstants.OPERATING_DAYS / 1000.0;

        assertEquals(Math.round(expectedDiesel * 100.0) / 100.0, result.getEmisionesDieselTon(), 0.01);
        assertEquals(Math.round(expectedElectric * 100.0) / 100.0, result.getEmisionesElectricoTon(), 0.01);
        assertTrue(result.getAhorroTon() > 0);
        assertEquals(result.getEmisionesDieselTon() - result.getEmisionesElectricoTon(),
                result.getAhorroTon(), 0.01);
    }

    @Test
    void executeShouldAssignHighestScoreToRouteWithMaxRaw() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route big = buildRoute("R1", "1", "Big Route", "AG", 30.0);
        Route small = buildRoute("R2", "2", "Small Route", "AG", 10.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(big, small));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "AAA", 5),
                                    buildTripsPerDay("R2", "2", "BBB", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);

        // First result should have score 100 (the biggest route wins)
        assertEquals(100.0, results.get(0).getScore(), 0.01);
        assertEquals("R1", results.get(0).getRouteId());
        assertTrue(results.get(1).getScore() < 100.0);
    }

    @Test
    void executeShouldAssignPrioridadAltaWhenScoreAbove70() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r = buildRoute("R1", "1", "Route 1", "AG", 20.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "AAA", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);
        // Single route always gets score 100 → Alta
        assertEquals("Alta", results.get(0).getPrioridad());
    }

    @Test
    void executeShouldSortByScoreDescending() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r1 = buildRoute("R1", "1", "Big Route", "AG", 50.0);
        Route r2 = buildRoute("R2", "2", "Small Route", "AG", 5.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r2, r1));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "AAA", 5),
                                    buildTripsPerDay("R2", "2", "BBB", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);

        assertTrue(results.get(0).getScore() >= results.get(1).getScore());
        assertEquals("R1", results.get(0).getRouteId());
    }

    @Test
    void executeShouldPopulateDetallesWithFallbackWhenNoAfluencia() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r = buildRoute("R1", "1", "Route 1", "AG", 20.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "AAA", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);
        var detalles = results.get(0).getDetallesPorDia();
        assertNotNull(detalles);
        // All days have trips=5 → fallback pasajeros = 5 * 79 = 395
        assertEquals(5, detalles.get("lunes").getViajes());
        assertEquals(395.0, detalles.get("lunes").getPasajeros(), 0.01);
    }

    @Test
    void normalizeLinesaShouldMatchVariantFormats() {
        assertEquals("1", CalculateCo2SavingsUseCase.normalizeLinea("L1"));
        assertEquals("1", CalculateCo2SavingsUseCase.normalizeLinea("Línea 1"));
        assertEquals("1", CalculateCo2SavingsUseCase.normalizeLinea("linea 1"));
        assertEquals("1", CalculateCo2SavingsUseCase.normalizeLinea("  1  "));
        assertEquals("12", CalculateCo2SavingsUseCase.normalizeLinea("L12"));
        assertEquals("", CalculateCo2SavingsUseCase.normalizeLinea(null));
    }

    @Test
    void executeShouldPopulateDetallesWhenAfluenciaExists() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r = buildRoute("R1", "13", "Route 1", "AG", 20.0);
        RouteTripsPerDay trips = buildTripsPerDay("R1", "13", "AAA", 3);

        AfluenciaResumen lunes = new AfluenciaResumen("13", 2, 1500.0);
        AfluenciaResumen martes = new AfluenciaResumen("13", 3, 1200.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r));
        when(tripRepository.findCountGroupedByRouteAndDay()).thenReturn(List.of(trips));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(List.of(lunes, martes));

        List<Co2SavingsResult> results = useCase.execute(1L);
        assertNotNull(results.get(0).getDetallesPorDia());
        assertNotNull(results.get(0).getDetallesPorDia().get("lunes"));
        assertEquals(3, results.get(0).getDetallesPorDia().get("lunes").getViajes());
        assertEquals(1500.0, results.get(0).getDetallesPorDia().get("lunes").getPasajeros(), 0.01);
    }

    @Test
    void executeShouldSetAgencyColorFromTripsData() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r = buildRoute("R1", "1", "Route 1", "AG", 20.0);
        RouteTripsPerDay trips = buildTripsPerDay("R1", "1", "009B3A", 5);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r));
        when(tripRepository.findCountGroupedByRouteAndDay()).thenReturn(List.of(trips));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        List<Co2SavingsResult> results = useCase.execute(1L);
        assertEquals("009B3A", results.get(0).getAgencyColor());
    }

    @Test
    void executeShouldReturnResultWithAllRequiredFields() {
        BusModel model = buildElectricModel(1L, 1.0);
        Route r = buildRoute("R1", "1", "Route 1", "SEMOVI", 20.0);

        when(busModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(routeRepository.findAllWithDistance()).thenReturn(List.of(r));
        when(tripRepository.findCountGroupedByRouteAndDay())
                .thenReturn(List.of(buildTripsPerDay("R1", "1", "AAA", 5)));
        when(afluenciaRepository.findGroupedByLineaAndDow()).thenReturn(Collections.emptyList());

        Co2SavingsResult result = useCase.execute(1L).get(0);

        assertNotNull(result.getRouteId());
        assertNotNull(result.getRouteName());
        assertNotNull(result.getAgencyId());
        assertNotNull(result.getDistanciaKm());
        assertNotNull(result.getEmisionesDieselTon());
        assertNotNull(result.getEmisionesElectricoTon());
        assertNotNull(result.getAhorroTon());
        assertNotNull(result.getScore());
        assertNotNull(result.getPrioridad());
    }
}
