package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Agency;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.AgencyRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;

import java.io.InputStream;

@ApplicationScoped
public class ImportAgencyUseCase {

    private final AgencyRepository agencyRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final FrequencyRepository frequencyRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportAgencyUseCase(AgencyRepository agencyRepository,
                               RouteRepository routeRepository,
                               TripRepository tripRepository,
                               StopTimeRepository stopTimeRepository,
                               FrequencyRepository frequencyRepository,
                               CsvParser csvParser) {
        this.agencyRepository = agencyRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.frequencyRepository = frequencyRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across cascade deletes + bulk inserts.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"agency_id", "agency_name", "agency_url",
                "agency_timezone", "agency_lang", "agency_color"};

        CsvParser.ParseResult<Agency> result = csvParser.parse(csvFile, headers, this::mapRow);

        frequencyRepository.deleteAll();
        stopTimeRepository.deleteAll();
        tripRepository.deleteAll();
        routeRepository.deleteAll();
        agencyRepository.deleteAll();

        int imported = agencyRepository.createAll(result.getItems());
        return new CsvImportResult("agency", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Agency mapRow(String[] row) {
        Agency agency = new Agency();
        agency.setAgencyId(row[0].trim());
        agency.setAgencyName(row[1].trim());
        agency.setAgencyUrl(row[2].trim());
        agency.setAgencyTimezone(row[3].trim());
        agency.setAgencyLang(row[4].trim());
        agency.setAgencyColor(row.length > 5 ? row[5].trim() : null);
        return agency;
    }
}
