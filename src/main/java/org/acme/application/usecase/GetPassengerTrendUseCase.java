package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.PassengerTrendPoint;
import org.acme.domain.repository.AfluenciaMetrobusRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class GetPassengerTrendUseCase {

    // DAYOFWEEK: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    private static final Map<Integer, String> DAY_LABELS = new TreeMap<>();
    static {
        DAY_LABELS.put(2, "Lun");
        DAY_LABELS.put(3, "Mar");
        DAY_LABELS.put(4, "Mié");
        DAY_LABELS.put(5, "Jue");
        DAY_LABELS.put(6, "Vie");
        DAY_LABELS.put(7, "Sáb");
        DAY_LABELS.put(1, "Dom");
    }

    private final AfluenciaMetrobusRepository afluenciaRepository;

    @Inject
    public GetPassengerTrendUseCase(AfluenciaMetrobusRepository afluenciaRepository) {
        this.afluenciaRepository = afluenciaRepository;
    }

    public List<PassengerTrendPoint> execute() {
        List<Object[]> rows = afluenciaRepository.findAvgByDayOfWeek();

        // Build a map from dayOfWeek -> avgPassengers from actual data
        Map<Integer, Double> dataByDow = new TreeMap<>();
        for (Object[] row : rows) {
            int dow = ((Number) row[0]).intValue();
            double avg = ((Number) row[1]).doubleValue();
            dataByDow.put(dow, avg);
        }

        // Return Mon–Sun in order, filling 0 for days with no data
        List<PassengerTrendPoint> result = new ArrayList<>();
        int[] order = {2, 3, 4, 5, 6, 7, 1};
        for (int dow : order) {
            double avg = dataByDow.getOrDefault(dow, 0.0);
            result.add(new PassengerTrendPoint(DAY_LABELS.get(dow), Math.round(avg)));
        }
        return result;
    }
}
