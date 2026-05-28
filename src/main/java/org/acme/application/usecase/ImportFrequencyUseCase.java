package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Frequency;
import org.acme.domain.repository.FrequencyRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportFrequencyUseCase {

    private final FrequencyRepository frequencyRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportFrequencyUseCase(FrequencyRepository frequencyRepository,
                                  CsvParser csvParser) {
        this.frequencyRepository = frequencyRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across delete + bulk insert.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"trip_id", "start_time", "end_time",
                "headway_secs", "exact_times"};

        CsvParser.ParseResult<Frequency> result = csvParser.parse(csvFile, headers, this::mapRow);

        frequencyRepository.deleteAll();

        List<Frequency> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Frequency f : result.getItems()) {
            if (seen.add(f.getTripId() + "|" + f.getStartTime())) deduped.add(f);
        }
        int imported = frequencyRepository.createAll(deduped);
        return new CsvImportResult("frequencies", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Frequency mapRow(String[] row) {
        Frequency frequency = new Frequency();
        frequency.setTripId(row[0].trim());
        frequency.setStartTime(row[1].trim());
        frequency.setEndTime(row[2].trim());
        frequency.setHeadwaySecs(Integer.parseInt(row[3].trim()));
        frequency.setExactTimes(
                row.length > 4 && !row[4].trim().isEmpty()
                        ? (byte) Double.parseDouble(row[4].trim())
                        : (byte) 0
        );
        return frequency;
    }
}
