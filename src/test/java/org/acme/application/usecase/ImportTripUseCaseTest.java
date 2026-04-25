package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Trip;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportTripUseCaseTest {

    private TripRepository tripRepository;
    private StopTimeRepository stopTimeRepository;
    private FrequencyRepository frequencyRepository;
    private CsvParser csvParser;
    private ImportTripUseCase useCase;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        stopTimeRepository = mock(StopTimeRepository.class);
        frequencyRepository = mock(FrequencyRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportTripUseCase(tripRepository, stopTimeRepository,
                frequencyRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Trip> trips = List.of(buildTrip("T1"));
        stubParser(trips, 1, Collections.emptyList());
        when(tripRepository.createAll(trips)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("trips", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Trip> trips = List.of(buildTrip("T1"), buildTrip("T2"));
        stubParser(trips, 3, List.of("Fila 3: error"));
        when(tripRepository.createAll(trips)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldCascadeDeleteInCorrectOrder() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(tripRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        InOrder inOrder = inOrder(frequencyRepository, stopTimeRepository, tripRepository);
        inOrder.verify(frequencyRepository).deleteAll();
        inOrder.verify(stopTimeRepository).deleteAll();
        inOrder.verify(tripRepository).deleteAll();
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(tripRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"route_id", "service_id", "trip_id", "shape_id",
                "trip_headsign", "trip_short_name", "direction_id"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(tripRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldHandleOptionalFields() {
        CsvParser realParser = new CsvParser();
        ImportTripUseCase realUseCase = new ImportTripUseCase(tripRepository,
                stopTimeRepository, frequencyRepository, realParser);
        when(tripRepository.createAll(anyList())).thenReturn(1);

        String csv = "route_id,service_id,trip_id,shape_id,trip_headsign,trip_short_name,direction_id\n"
                + "R1,SVC1,T1,,,,\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Trip>> captor = ArgumentCaptor.forClass(List.class);
        verify(tripRepository).createAll(captor.capture());
        Trip trip = captor.getValue().get(0);
        assertEquals("R1", trip.getRouteId());
        assertNull(trip.getShapeId());
        assertNull(trip.getTripHeadsign());
        assertNull(trip.getTripShortName());
        assertNull(trip.getDirectionId());
    }

    private Trip buildTrip(String id) {
        Trip t = new Trip();
        t.setTripId(id);
        t.setRouteId("R1");
        t.setServiceId("SVC1");
        return t;
    }

    private void stubParser(List<Trip> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Trip> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
