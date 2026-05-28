package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Trip;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportTripUseCase {

    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final FrequencyRepository frequencyRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportTripUseCase(TripRepository tripRepository,
                             StopTimeRepository stopTimeRepository,
                             FrequencyRepository frequencyRepository,
                             CsvParser csvParser) {
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.frequencyRepository = frequencyRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across cascade deletes + bulk inserts.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"route_id", "service_id", "trip_id", "shape_id",
                "trip_headsign", "trip_short_name", "direction_id"};

        CsvParser.ParseResult<Trip> result = csvParser.parse(csvFile, headers, this::mapRow);

        frequencyRepository.deleteAll();
        stopTimeRepository.deleteAll();
        tripRepository.deleteAll();

        List<Trip> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Trip t : result.getItems()) {
            if (seen.add(t.getTripId())) deduped.add(t);
        }
        int imported = tripRepository.createAll(deduped);
        return new CsvImportResult("trips", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Trip mapRow(String[] row) {
        Trip trip = new Trip();
        trip.setRouteId(row[0].trim());
        trip.setServiceId(row[1].trim());
        trip.setTripId(row[2].trim());

        String shapeId = row.length > 3 ? row[3].trim() : null;
        trip.setShapeId(shapeId != null && shapeId.isEmpty() ? null : shapeId);

        String headsign = row.length > 4 ? row[4].trim() : null;
        trip.setTripHeadsign(headsign != null && headsign.isEmpty() ? null : headsign);

        String shortName = row.length > 5 ? row[5].trim() : null;
        trip.setTripShortName(shortName != null && shortName.isEmpty() ? null : shortName);

        trip.setDirectionId(
                row.length > 6 && !row[6].trim().isEmpty()
                        ? (byte) Double.parseDouble(row[6].trim())
                        : (byte) 0
        );
        return trip;
    }
}
