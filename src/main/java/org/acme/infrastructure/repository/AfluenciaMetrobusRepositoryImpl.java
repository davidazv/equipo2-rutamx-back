package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.AfluenciaResumen;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.acme.infrastructure.entities.AfluenciaMetrobusEntity;
import org.acme.infrastructure.mapper.AfluenciaMetrobusMapper;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AfluenciaMetrobusRepositoryImpl implements AfluenciaMetrobusRepository {

    @Inject
    EntityManager entityManager;

    private static final String AFLUENCIA_BY_LINEA_AND_DOW_QUERY =
            "SELECT linea, DAYOFWEEK(fecha), AVG(afluencia) " +
            "FROM afluencia_metrobus " +
            "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
            "GROUP BY linea, DAYOFWEEK(fecha) " +
            "ORDER BY linea, DAYOFWEEK(fecha)";

    @Override
    public List<AfluenciaResumen> findGroupedByLineaAndDow() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(AFLUENCIA_BY_LINEA_AND_DOW_QUERY)
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

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM afluencia_metrobus").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<AfluenciaMetrobus> items) {
        int count = 0;
        for (AfluenciaMetrobus item : items) {
            AfluenciaMetrobusEntity entity = AfluenciaMetrobusMapper.toEntity(item);
            entityManager.persist(entity);
            if (++count % 500 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
