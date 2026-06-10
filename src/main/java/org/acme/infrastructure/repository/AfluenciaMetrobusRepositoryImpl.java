package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.infrastructure.mapper.AfluenciaMetrobusMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AfluenciaMetrobusRepositoryImpl implements AfluenciaMetrobusRepository {

    @Inject
    EntityManager entityManager;

    private static final String AFLUENCIA_BY_LINEA_AND_DOW_QUERY =
            "SELECT linea, DAYOFWEEK(fecha), AVG(afluencia) " +
            "FROM afluencia_metrobus " +
            "WHERE fecha >= :oneYearAgo " +
            "GROUP BY linea, DAYOFWEEK(fecha) " +
            "ORDER BY linea, DAYOFWEEK(fecha)";

    @Override
    public List<AfluenciaResumen> findGroupedByLineaAndDow() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(AFLUENCIA_BY_LINEA_AND_DOW_QUERY)
                .setParameter("oneYearAgo", LocalDate.now().minusYears(1))
                .getResultList();

        List<AfluenciaResumen> result = new ArrayList<>();
        for (Object[] row : rows) {
            AfluenciaResumen r = new AfluenciaResumen();
            r.setLinea((String) row[0]);
            r.setDayOfWeek(((Number) row[1]).intValue());
            r.setTotalAfluencia(((Number) row[2]).doubleValue());
            result.add(r);
        }
        return result;
    }

    private static final String AVG_DEMAND_BY_LINEA_QUERY =
            "SELECT day_type, AVG(daily_total) AS avg_demand " +
            "FROM ( " +
            "  SELECT " +
            "    CASE WHEN DAYOFWEEK(fecha) BETWEEN 2 AND 6 THEN 'weekday' " +
            "         WHEN DAYOFWEEK(fecha) = 7 THEN 'saturday' " +
            "         ELSE 'sunday' END AS day_type, " +
            "    SUM(afluencia) AS daily_total " +
            "  FROM afluencia_metrobus " +
            "  WHERE linea = ?1 " +
            "  GROUP BY fecha " +
            ") daily_sums " +
            "GROUP BY day_type";

    @Override
    public Map<String, Double> findAvgDemandByLinea(String linea) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(AVG_DEMAND_BY_LINEA_QUERY)
                .setParameter(1, linea)
                .getResultList();

        Map<String, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], ((Number) row[1]).doubleValue());
        }
        return result;
    }

    private static final String AVG_DAILY_PASSENGERS_QUERY =
            "SELECT AVG(daily_total) " +
            "FROM (SELECT SUM(afluencia) AS daily_total FROM afluencia_metrobus GROUP BY fecha) daily_sums";

    @Override
    public double findAvgDailyPassengers() {
        Object result = entityManager
                .createNativeQuery(AVG_DAILY_PASSENGERS_QUERY)
                .getSingleResult();
        return result == null ? 0.0 : ((Number) result).doubleValue();
    }

    private static final String AVG_BY_DOW_QUERY =
            "SELECT DAYOFWEEK(fecha) AS dow, AVG(daily_total) AS avg_passengers " +
            "FROM (SELECT fecha, SUM(afluencia) AS daily_total FROM afluencia_metrobus GROUP BY fecha) daily_sums " +
            "GROUP BY DAYOFWEEK(fecha) " +
            "ORDER BY dow";

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> findAvgByDayOfWeek() {
        return entityManager
                .createNativeQuery(AVG_BY_DOW_QUERY)
                .getResultList();
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM afluencia_metrobus").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<AfluenciaMetrobus> items) {
        return BatchPersister.persistAll(entityManager, items, 500, AfluenciaMetrobusMapper::toEntity);
    }
}
