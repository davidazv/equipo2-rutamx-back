package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.AfluenciaMetrobus;
import org.acme.domain.models.DayType;
import org.acme.domain.repository.AfluenciaMetrobusRepository;

import java.math.BigDecimal;
import org.acme.infrastructure.entities.AfluenciaMetrobusEntity;
import org.acme.infrastructure.mapper.AfluenciaMetrobusMapper;

import java.util.List;

@ApplicationScoped
public class AfluenciaMetrobusRepositoryImpl implements AfluenciaMetrobusRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM afluencia_metrobus").executeUpdate();
    }

    @Override
    public BigDecimal findAverageDailyDemand(String linea, DayType dayType) {
        String dayFilter = switch (dayType) {
            case WEEKDAY  -> "DAYOFWEEK(fecha) BETWEEN 2 AND 6";
            case SATURDAY -> "DAYOFWEEK(fecha) = 7";
            case SUNDAY   -> "DAYOFWEEK(fecha) = 1";
        };

        String sql = "SELECT SUM(afluencia) / COUNT(DISTINCT fecha) " +
                     "FROM afluencia_metrobus " +
                     "WHERE linea = ?1 AND " + dayFilter;

        Object result = entityManager.createNativeQuery(sql)
                .setParameter(1, linea)
                .getSingleResult();

        if (result == null) return null;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
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
