package org.acme.domain.repository;

import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import java.util.List;
import java.util.Map;

public interface AfluenciaMetrobusRepository {
    void deleteAll();
    int createAll(List<AfluenciaMetrobus> items);
    List<AfluenciaResumen> findGroupedByLineaAndDow();
    /**
     * Returns the average daily passenger demand grouped by day type for a given linea.
     * Keys: "weekday", "saturday", "sunday". Empty map if no data found.
     * Used by HU12 (RecommendBusModelUseCase).
     */
    Map<String, Double> findAvgDemandByLinea(String linea);
    /** Average of daily totals (SUM per fecha) across all lines. Returns 0 if no data. */
    double findAvgDailyPassengers();
    /** Returns (dayOfWeek int [1=Sun..7=Sat], avgPassengers double) pairs ordered by dayOfWeek. */
    List<Object[]> findAvgByDayOfWeek();
}
