package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Shape;
import org.acme.domain.repository.ShapeRepository;

import java.io.InputStream;
import java.math.BigDecimal;

@ApplicationScoped
public class ImportShapeUseCase {

    private final ShapeRepository shapeRepository;
    private final CsvParser csvParser;

    @Inject
    public ImportShapeUseCase(ShapeRepository shapeRepository,
                              CsvParser csvParser) {
        this.shapeRepository = shapeRepository;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across delete + bulk insert.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"shape_id", "shape_pt_lat", "shape_pt_lon",
                "shape_pt_sequence", "shape_dist_traveled"};

        CsvParser.ParseResult<Shape> result = csvParser.parse(csvFile, headers, this::mapRow);

        shapeRepository.deleteAll();

        int imported = shapeRepository.createAll(result.getItems());
        return new CsvImportResult("shapes", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private Shape mapRow(String[] row) {
        Shape shape = new Shape();
        shape.setShapeId(row[0].trim());
        shape.setShapePtLat(new BigDecimal(row[1].trim()));
        shape.setShapePtLon(new BigDecimal(row[2].trim()));
        shape.setShapePtSequence(Integer.parseInt(row[3].trim()));
        shape.setShapeDistTraveled(
                !row[4].trim().isEmpty()
                        ? new BigDecimal(row[4].trim())
                        : null
        );
        return shape;
    }
}
