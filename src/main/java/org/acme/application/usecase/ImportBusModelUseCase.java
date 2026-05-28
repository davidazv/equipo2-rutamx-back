package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.FuelType;
import org.acme.domain.repository.BusModelRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportBusModelUseCase {

    private final BusModelRepository busModelRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportBusModelUseCase(BusModelRepository busModelRepository,
                                 CsvParser csvParser) {
        this.busModelRepository = busModelRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across delete + bulk insert.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"name", "manufacturer", "fuel_type", "autonomy_km",
                "passenger_capacity", "unit_cost_usd", "battery_capacity_kwh",
                "energy_consumption_kwh_km", "fuel_consumption_l_km",
                "maintenance_cost_per_km", "co2_emissions_g_km"};

        CsvParser.ParseResult<BusModel> result = csvParser.parse(csvFile, headers, this::mapRow);

        busModelRepository.deleteAll();

        List<BusModel> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (BusModel m : result.getItems()) {
            if (seen.add(m.getName())) deduped.add(m);
        }
        int imported = busModelRepository.createAll(deduped);
        return new CsvImportResult("bus_model", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private BusModel mapRow(String[] row) {
        BusModel model = new BusModel();
        model.setName(row[0].trim());
        model.setManufacturer(row[1].trim());
        model.setFuelType(FuelType.valueOf(row[2].trim().toUpperCase()));
        model.setAutonomyKm(new BigDecimal(row[3].trim()));
        model.setPassengerCapacity(Integer.parseInt(row[4].trim()));
        model.setUnitCostUsd(new BigDecimal(row[5].trim()));
        model.setBatteryCapacityKwh(parseOrZero(row[6].trim()));
        model.setEnergyConsumptionKwhKm(parseOrZero(row[7].trim()));
        model.setFuelConsumptionLKm(parseOrZero(row[8].trim()));
        model.setMaintenanceCostPerKm(new BigDecimal(row[9].trim()));
        model.setCo2EmissionsGKm(new BigDecimal(row[10].trim()));
        return model;
    }

    private static BigDecimal parseOrZero(String val) {
        return val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
    }
}
