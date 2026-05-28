package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.StopTime;
import org.acme.domain.repository.StopTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportStopTimeUseCaseTest {

    private StopTimeRepository stopTimeRepository;
    private CsvParser csvParser;
    private ImportStopTimeUseCase useCase;

    @BeforeEach
    void setUp() {
        stopTimeRepository = mock(StopTimeRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportStopTimeUseCase(stopTimeRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<StopTime> stopTimes = List.of(buildStopTime("T1", "S1"));
        stubParser(stopTimes, 1, Collections.emptyList());
        when(stopTimeRepository.createAll(stopTimes)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("stop_times", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        StopTime st1 = buildStopTime("T1", "S1");
        StopTime st2 = buildStopTime("T1", "S2");
        st2.setStopSequence(2);
        List<StopTime> items = List.of(st1, st2);
        stubParser(items, 3, List.of("Fila 3: bad seq"));
        when(stopTimeRepository.createAll(items)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldDeleteBeforeInsert() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopTimeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        var inOrder = inOrder(stopTimeRepository);
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(stopTimeRepository).createAll(anyList());
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopTimeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"trip_id", "timepoint", "stop_id", "stop_sequence",
                "arrival_time", "departure_time"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopTimeRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldHandleOptionalTimepoint() {
        CsvParser realParser = new CsvParser();
        ImportStopTimeUseCase realUseCase = new ImportStopTimeUseCase(stopTimeRepository, realParser);
        when(stopTimeRepository.createAll(anyList())).thenReturn(1);

        String csv = "trip_id,timepoint,stop_id,stop_sequence,arrival_time,departure_time\n"
                + "T1,,S1,1,08:00:00,08:01:00\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StopTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(stopTimeRepository).createAll(captor.capture());
        StopTime st = captor.getValue().get(0);
        assertNull(st.getTimepoint());
        assertEquals("T1", st.getTripId());
        assertEquals(1, st.getStopSequence());
    }

    private StopTime buildStopTime(String tripId, String stopId) {
        StopTime st = new StopTime();
        st.setTripId(tripId);
        st.setStopId(stopId);
        st.setStopSequence(1);
        st.setArrivalTime("08:00:00");
        st.setDepartureTime("08:01:00");
        return st;
    }

    private void stubParser(List<StopTime> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<StopTime> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
