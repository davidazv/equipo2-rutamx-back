package org.acme.application.usecase;

import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Shape;
import org.acme.domain.repository.ShapeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportShapeUseCaseTest {

    private ShapeRepository shapeRepository;
    private CsvParser csvParser;
    private ImportShapeUseCase useCase;

    @BeforeEach
    void setUp() {
        shapeRepository = mock(ShapeRepository.class);
        csvParser = mock(CsvParser.class);
        useCase = new ImportShapeUseCase(shapeRepository, csvParser);
    }

    @Test
    void executeShouldParseAndImportSuccessfully() {
        List<Shape> shapes = List.of(buildShape("SH1", 1));
        stubParser(shapes, 1, Collections.emptyList());
        when(shapeRepository.createAll(shapes)).thenReturn(1);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(1, result.getImportedRows());
        assertEquals("shapes", result.getTableName());
    }

    @Test
    void executeShouldReturnCorrectImportResult() {
        List<Shape> shapes = List.of(buildShape("SH1", 1), buildShape("SH1", 2));
        stubParser(shapes, 3, List.of("Fila 3: bad coord"));
        when(shapeRepository.createAll(shapes)).thenReturn(2);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getImportedRows());
        assertEquals(1, result.getSkippedRows());
    }

    @Test
    void executeShouldDeleteBeforeInsert() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(shapeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        var inOrder = inOrder(shapeRepository);
        inOrder.verify(shapeRepository).deleteAll();
        inOrder.verify(shapeRepository).createAll(anyList());
    }

    @Test
    void executeShouldPassCorrectHeadersToCsvParser() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(shapeRepository.createAll(anyList())).thenReturn(0);

        useCase.execute(dummyStream());

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(csvParser).parse(any(InputStream.class), captor.capture(), any());
        assertArrayEquals(new String[]{"shape_id", "shape_pt_lat", "shape_pt_lon",
                "shape_pt_sequence", "shape_dist_traveled"}, captor.getValue());
    }

    @Test
    void executeShouldHandleEmptyParseResult() {
        stubParser(Collections.emptyList(), 0, Collections.emptyList());
        when(shapeRepository.createAll(Collections.emptyList())).thenReturn(0);

        CsvImportResult result = useCase.execute(dummyStream());

        assertEquals(0, result.getImportedRows());
    }

    @Test
    void mapRowShouldHandleOptionalDistTraveled() {
        CsvParser realParser = new CsvParser();
        ImportShapeUseCase realUseCase = new ImportShapeUseCase(shapeRepository, realParser);
        when(shapeRepository.createAll(anyList())).thenReturn(1);

        String csv = "shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled\n"
                + "SH1,19.345,-99.065,1,\n";
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        realUseCase.execute(input);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Shape>> captor = ArgumentCaptor.forClass(List.class);
        verify(shapeRepository).createAll(captor.capture());
        Shape shape = captor.getValue().get(0);
        assertNull(shape.getShapeDistTraveled());
        assertEquals("SH1", shape.getShapeId());
    }

    private Shape buildShape(String id, int seq) {
        Shape s = new Shape();
        s.setShapeId(id);
        s.setShapePtLat(new BigDecimal("19.345"));
        s.setShapePtLon(new BigDecimal("-99.065"));
        s.setShapePtSequence(seq);
        s.setShapeDistTraveled(new BigDecimal("0.0"));
        return s;
    }

    private void stubParser(List<Shape> items, int totalRows, List<String> errors) {
        CsvParser.ParseResult<Shape> parseResult =
                new CsvParser.ParseResult<>(items, totalRows, errors);
        doReturn(parseResult).when(csvParser).parse(any(InputStream.class), any(String[].class), any());
    }

    private InputStream dummyStream() {
        return new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8));
    }
}
