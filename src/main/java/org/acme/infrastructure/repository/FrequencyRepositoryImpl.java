package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Frequency;

import java.math.BigDecimal;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.infrastructure.entities.FrequencyEntity;
import org.acme.infrastructure.entities.TripEntity;
import org.acme.infrastructure.mapper.FrequencyMapper;

import java.util.List;

@ApplicationScoped
public class FrequencyRepositoryImpl implements FrequencyRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM FrequencyEntity").executeUpdate();
    }

    @Override
    public Double findAverageHeadwayByRouteShortName(String routeShortName) {
        String sql = "SELECT AVG(f.headway_secs) " +
                     "FROM frequencies f " +
                     "INNER JOIN trips t ON t.trip_id = f.trip_id " +
                     "INNER JOIN routes r ON r.route_id = t.route_id " +
                     "WHERE r.agency_id = 'MB' AND r.route_short_name = ?1";

        Object result = entityManager.createNativeQuery(sql)
                .setParameter(1, routeShortName)
                .getSingleResult();

        if (result == null) return null;
        if (result instanceof BigDecimal) return ((BigDecimal) result).doubleValue();
        return ((Number) result).doubleValue();
    }

    @Override
    @Transactional
    public int createAll(List<Frequency> items) {
        int count = 0;
        for (Frequency item : items) {
            FrequencyEntity entity = FrequencyMapper.toEntity(item);
            TripEntity trip = entityManager.getReference(TripEntity.class, item.getTripId());
            entity.setTrip(trip);
            entityManager.persist(entity);
            if (++count % 200 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
