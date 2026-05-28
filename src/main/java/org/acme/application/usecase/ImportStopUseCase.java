package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Stop;
import org.acme.domain.repository.StopRepository;
import org.acme.domain.repository.StopTimeRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportStopUseCase {

    private final StopRepository stopRepository;
    private final StopTimeRepository stopTimeRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportStopUseCase(StopRepository stopRepository,
                             StopTimeRepository stopTimeRepository,
                             CsvParser csvParser) {
        this.stopRepository = stopRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across cascade deletes + bulk inserts.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"stop_id", "stop_name", "stop_lat", "stop_lon",
                "zone_id", "wheelchair_boarding"};

        CsvParser.ParseResult<Stop> result = csvParser.parse(csvFile, headers, this::mapRow);

        stopTimeRepository.deleteAll();
        stopRepository.deleteAll();

        List<Stop> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Stop s : result.getItems()) {
            if (seen.add(s.getStopId())) deduped.add(s);
        }
        int imported = stopRepository.createAll(deduped);
        return new CsvImportResult("stops", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Stop mapRow(String[] row) {
        BigDecimal lat = new BigDecimal(row[2].trim());
        BigDecimal lon = new BigDecimal(row[3].trim());
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                || lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
            return null;
        }
        Stop stop = new Stop();
        stop.setStopId(row[0].trim());
        stop.setStopName(row[1].trim());
        stop.setStopLat(lat);
        stop.setStopLon(lon);
        stop.setZoneId(row[4].trim());
        stop.setWheelchairBoarding(
                row.length > 5 && !row[5].trim().isEmpty()
                        ? (byte) Double.parseDouble(row[5].trim())
                        : null
        );
        return stop;
    }
}
