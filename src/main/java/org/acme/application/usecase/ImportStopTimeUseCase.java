package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.StopTime;
import org.acme.domain.repository.StopTimeRepository;

import java.io.InputStream;
import java.util.List;

@ApplicationScoped
public class ImportStopTimeUseCase {

    private final StopTimeRepository stopTimeRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportStopTimeUseCase(StopTimeRepository stopTimeRepository,
                                 CsvParser csvParser) {
        this.stopTimeRepository = stopTimeRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across delete + bulk insert.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"trip_id", "timepoint", "stop_id", "stop_sequence",
                "arrival_time", "departure_time"};

        CsvParser.ParseResult<StopTime> result = csvParser.parse(csvFile, headers, this::mapRow);

        stopTimeRepository.deleteAll();

        List<StopTime> deduped = ImportSupport.dedupBy(result.getItems(),
                st -> st.getTripId() + "|" + st.getStopSequence());
        int imported = stopTimeRepository.createAll(deduped);
        return new CsvImportResult("stop_times", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private StopTime mapRow(String[] row) {
        StopTime stopTime = new StopTime();
        stopTime.setTripId(row[0].trim());
        stopTime.setTimepoint(
                !row[1].trim().isEmpty()
                        ? (byte) Double.parseDouble(row[1].trim())
                        : null
        );
        stopTime.setStopId(row[2].trim());
        int seq = Integer.parseInt(row[3].trim());
        if (seq <= 0) return null;
        stopTime.setStopSequence(seq);
        stopTime.setArrivalTime(row[4].trim());
        stopTime.setDepartureTime(row[5].trim());
        return stopTime;
    }
}
