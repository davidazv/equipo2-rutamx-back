package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Stop;
import org.acme.domain.repository.StopRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportStopUseCaseTest {

    private StopRepository stopRepository;
    private StopTimeRepository stopTimeRepository;
    private CsvParser csvParser;
    private ImportStopUseCase useCase;

    @BeforeEach
    void setUp() {
        stopRepository = mock(StopRepository.class);
        stopTimeRepository = mock(StopTimeRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportStopUseCase(stopRepository, stopTimeRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Stop> stops = List.of(buildStop("S1"));
        stubParser(stops, 1, Collections.emptyList());
        when(stopRepository.createAll(stops)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("stops", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Stop> stops = List.of(buildStop("S1"), buildStop("S2"));
        stubParser(stops, 3, List.of("Fila 3: bad coord"));
        when(stopRepository.createAll(stops)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldCascadeDeleteInCorrectOrder() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        InOrder inOrder = inOrder(stopTimeRepository, stopRepository);
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(stopRepository).deleteAll();
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"stop_id", "stop_name", "stop_lat", "stop_lon",
                "zone_id", "wheelchair_boarding"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(stopRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldHandleOptionalWheelchairBoarding() {
        CsvParser realParser = new CsvParser();
        ImportStopUseCase realUseCase = new ImportStopUseCase(stopRepository,
                stopTimeRepository, realParser);
        when(stopRepository.createAll(anyList())).thenReturn(1);

        String csv = "stop_id,stop_name,stop_lat,stop_lon,zone_id,wheelchair_boarding\n"
                + "S1,Stop One,19.345,  -99.065,Z1,\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Stop>> captor = ArgumentCaptor.forClass(List.class);
        verify(stopRepository).createAll(captor.capture());
        assertNull(captor.getValue().get(0).getWheelchairBoarding());
    }

    private Stop buildStop(String id) {
        Stop s = new Stop();
        s.setStopId(id);
        s.setStopName("Stop " + id);
        s.setStopLat(new BigDecimal("19.345"));
        s.setStopLon(new BigDecimal("-99.065"));
        s.setZoneId("Z1");
        return s;
    }

    private void stubParser(List<Stop> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Stop> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
