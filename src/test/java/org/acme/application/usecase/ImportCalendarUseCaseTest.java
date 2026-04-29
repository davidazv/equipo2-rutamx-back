package org.acme.application.usecase;

import org.acme.domain.models.Calendar;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.repository.CalendarRepository;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportCalendarUseCaseTest {

    private CalendarRepository calendarRepository;
    private TripRepository tripRepository;
    private StopTimeRepository stopTimeRepository;
    private FrequencyRepository frequencyRepository;
    private CsvParser csvParser;
    private ImportCalendarUseCase useCase;

    @BeforeEach
    void setUp() {
        calendarRepository = mock(CalendarRepository.class);
        tripRepository = mock(TripRepository.class);
        stopTimeRepository = mock(StopTimeRepository.class);
        frequencyRepository = mock(FrequencyRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportCalendarUseCase(calendarRepository, tripRepository,
                stopTimeRepository, frequencyRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Calendar> calendars = List.of(buildCalendar("SVC1"));
        stubParser(calendars, 1, Collections.emptyList());
        when(calendarRepository.createAll(calendars)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("calendar", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Calendar> calendars = List.of(buildCalendar("S1"), buildCalendar("S2"));
        stubParser(calendars, 3, List.of("Fila 3: bad date"));
        when(calendarRepository.createAll(calendars)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldCascadeDeleteInCorrectOrder() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(calendarRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        InOrder inOrder = inOrder(frequencyRepository, stopTimeRepository,
                tripRepository, calendarRepository);
        inOrder.verify(frequencyRepository).deleteAll();
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(tripRepository).deleteAll();
        inOrder.verify(calendarRepository).deleteAll();
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(calendarRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"service_id", "monday", "tuesday", "wednesday",
                "thursday", "friday", "saturday", "sunday", "start_date", "end_date"},
                captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(calendarRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldParseCompactDateFormat() {
        // Use real CsvParser to test actual row mapping via execute()
        CsvParser realParser = new CsvParser();
        ImportCalendarUseCase realUseCase = new ImportCalendarUseCase(
                calendarRepository, tripRepository, stopTimeRepository,
                frequencyRepository, realParser);
        when(calendarRepository.createAll(anyList())).thenReturn(1);

        String csv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "SVC1,1,1,1,1,1,0,0,20260101,20261231\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        CsvImportResult result = realUseCase.execute(input);

        assertEquals(1, result.getImportedRows());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Calendar>> captor = ArgumentCaptor.forClass(List.class);
        verify(calendarRepository).createAll(captor.capture());
        Calendar cal = captor.getValue().get(0);
        assertEquals(LocalDate.of(2026, 1, 1), cal.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), cal.getEndDate());
    }

    @Test
    void mapRowShouldParseIsoDateFormat() {
        CsvParser realParser = new CsvParser();
        ImportCalendarUseCase realUseCase = new ImportCalendarUseCase(
                calendarRepository, tripRepository, stopTimeRepository,
                frequencyRepository, realParser);
        when(calendarRepository.createAll(anyList())).thenReturn(1);

        String csv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "SVC2,1,1,1,1,1,0,0,2026-01-01,2026-12-31\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        CsvImportResult result = realUseCase.execute(input);

        assertEquals(1, result.getImportedRows());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Calendar>> captor = ArgumentCaptor.forClass(List.class);
        verify(calendarRepository).createAll(captor.capture());
        Calendar cal = captor.getValue().get(0);
        assertEquals(LocalDate.of(2026, 1, 1), cal.getStartDate());
    }

    private Calendar buildCalendar(String serviceId) {
        Calendar c = new Calendar();
        c.setServiceId(serviceId);
        c.setMonday((byte) 1);
        c.setTuesday((byte) 1);
        c.setWednesday((byte) 1);
        c.setThursday((byte) 1);
        c.setFriday((byte) 1);
        c.setSaturday((byte) 0);
        c.setSunday((byte) 0);
        c.setStartDate(LocalDate.of(2026, 1, 1));
        c.setEndDate(LocalDate.of(2026, 12, 31));
        return c;
    }

    private void stubParser(List<Calendar> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Calendar> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
