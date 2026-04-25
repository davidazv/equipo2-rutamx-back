package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.models.CsvImportResult;
import org.acme.domain.models.Route;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.domain.repository.RouteRepository;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.domain.repository.TripRepository;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportRouteUseCase {

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final FrequencyRepository frequencyRepository;
    private final RouteColorAssigner routeColorAssigner;
    private final CsvParser csvParser;

    @Inject
    public ImportRouteUseCase(RouteRepository routeRepository,
                              TripRepository tripRepository,
                              StopTimeRepository stopTimeRepository,
                              FrequencyRepository frequencyRepository,
                              RouteColorAssigner routeColorAssigner,
                              CsvParser csvParser) {
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.frequencyRepository = frequencyRepository;
        this.routeColorAssigner = routeColorAssigner;
        this.csvParser = csvParser;
    }

    // Exception to Constitution rule: @Transactional here ensures atomicity
    // across cascade deletes + bulk inserts.
    @Transactional
    public CsvImportResult execute(InputStream csvFile) {
        String[] headers = {"route_id", "agency_id", "route_short_name",
                "route_long_name", "route_type", "route_color", "route_text_color"};

        CsvParser.ParseResult<Route> result = csvParser.parse(csvFile, headers, this::mapRow);

        List<Route> routes = result.getItems();
        assignMissingColors(routes);

        frequencyRepository.deleteAll();
        stopTimeRepository.deleteAll();
        tripRepository.deleteAll();
        routeRepository.deleteAll();

        int imported = routeRepository.createAll(routes);
        return new CsvImportResult("routes", result.getTotalRows(), imported,
                result.getTotalRows() - imported, result.getErrors());
    }

    private void assignMissingColors(List<Route> routes) {
        Set<String> usedColors = new HashSet<>();
        for (Route route : routes) {
            String color = route.getRouteColor();
            if (color != null && !color.isEmpty()) {
                usedColors.add(color.toUpperCase());
            }
        }

        for (Route route : routes) {
            String color = route.getRouteColor();
            if (color == null || color.isEmpty()) {
                String assigned = routeColorAssigner.assignColor(usedColors);
                route.setRouteColor(assigned);
                usedColors.add(assigned.toUpperCase());
            }
        }
    }

    private Route mapRow(String[] row) {
        Route route = new Route();
        route.setRouteId(row[0].trim());
        route.setAgencyId(row[1].trim());
        route.setRouteShortName(row[2].trim());
        route.setRouteLongName(row[3].trim());
        route.setRouteType(Integer.parseInt(row[4].trim()));

        String routeColor = row.length > 5 ? row[5].trim() : null;
        route.setRouteColor(routeColor != null && routeColor.isEmpty() ? null : routeColor);

        String routeTextColor = row.length > 6 ? row[6].trim() : null;
        route.setRouteTextColor(routeTextColor != null && routeTextColor.isEmpty() ? null : routeTextColor);

        return route;
    }
}
