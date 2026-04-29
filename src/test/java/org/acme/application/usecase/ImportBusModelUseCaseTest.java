package org.acme.application.usecase;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportBusModelUseCaseTest {

    private BusModelRepository busModelRepository;
    private CsvParser csvParser;
    private ImportBusModelUseCase useCase;

    @BeforeEach
    void setUp() {
        busModelRepository = mock(BusModelRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportBusModelUseCase(busModelRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<BusModel> models = List.of(buildModel("Yutong E12", FuelType.ELECTRIC));
        stubParser(models, 1, Collections.emptyList());
        when(busModelRepository.createAll(models)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("bus_model", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<BusModel> models = List.of(buildModel("M1", FuelType.ELECTRIC), buildModel("M2", FuelType.DIESEL));
        stubParser(models, 3, List.of("Fila 3: error"));
        when(busModelRepository.createAll(models)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldDeleteBeforeInsert() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(busModelRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        var inOrder = inOrder(busModelRepository);
        inOrder.verify(busModelRepository).deleteAll();
        inOrder.verify(busModelRepository).createAll(anyList());
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(busModelRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"name", "manufacturer", "fuel_type", "autonomy_km",
                "passenger_capacity", "unit_cost_usd", "battery_capacity_kwh",
                "energy_consumption_kwh_km", "fuel_consumption_l_km",
                "maintenance_cost_per_km", "co2_emissions_g_km"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(busModelRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    private BusModel buildModel(String name, FuelType fuelType) {
        BusModel m = new BusModel();
        m.setName(name);
        m.setManufacturer("Yutong");
        m.setFuelType(fuelType);
        m.setAutonomyKm(new BigDecimal("300"));
        m.setPassengerCapacity(85);
        m.setUnitCostUsd(new BigDecimal("420000"));
        m.setBatteryCapacityKwh(new BigDecimal("352.08"));
        m.setEnergyConsumptionKwhKm(new BigDecimal("1.0"));
        m.setFuelConsumptionLKm(new BigDecimal("0.0"));
        m.setMaintenanceCostPerKm(new BigDecimal("0.12"));
        m.setCo2EmissionsGKm(new BigDecimal("0"));
        return m;
    }

    private void stubParser(List<BusModel> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<BusModel> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
