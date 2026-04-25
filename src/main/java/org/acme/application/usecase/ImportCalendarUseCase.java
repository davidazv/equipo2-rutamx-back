package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Calendar;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.CalendarRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class ImportCalendarUseCase {

    private static final DateTimeFormatter COMPACT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CalendarRepository calendarRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final FrequencyRepository frequencyRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportCalendarUseCase(CalendarRepository calendarRepository,
                                 TripRepository tripRepository,
                                 StopTimeRepository stopTimeRepository,
                                 FrequencyRepository frequencyRepository,
                                 CsvParser csvParser) {
        this.calendarRepository = calendarRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.frequencyRepository = frequencyRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across cascade deletes + bulk inserts.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"service_id", "monday", "tuesday", "wednesday",
                "thursday", "friday", "saturday", "sunday", "start_date", "end_date"};

        CsvParser.ParseResult<Calendar> result = csvParser.parse(csvFile, headers, this::mapRow);

        frequencyRepository.deleteAll();
        stopTimeRepository.deleteAll();
        tripRepository.deleteAll();
        calendarRepository.deleteAll();

        int imported = calendarRepository.createAll(result.getItems());
        return new CsvImportResult("calendar", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Calendar mapRow(String[] row) {
        Calendar calendar = new Calendar();
        calendar.setServiceId(row[0].trim());
        calendar.setMonday(Byte.parseByte(row[1].trim()));
        calendar.setTuesday(Byte.parseByte(row[2].trim()));
        calendar.setWednesday(Byte.parseByte(row[3].trim()));
        calendar.setThursday(Byte.parseByte(row[4].trim()));
        calendar.setFriday(Byte.parseByte(row[5].trim()));
        calendar.setSaturday(Byte.parseByte(row[6].trim()));
        calendar.setSunday(Byte.parseByte(row[7].trim()));
        calendar.setStartDate(parseDate(row[8].trim()));
        calendar.setEndDate(parseDate(row[9].trim()));
        return calendar;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, COMPACT_FORMAT);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(value, ISO_FORMAT);
        }
    }
}
