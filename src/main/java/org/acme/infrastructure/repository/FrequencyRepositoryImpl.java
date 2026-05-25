package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Frequency;
import org.acme.domain.repository.FrequencyRepository;
import org.acme.infrastructure.entities.FrequencyEntity;
import org.acme.infrastructure.entities.TripEntity;
import org.acme.infrastructure.mapper.FrequencyMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Frequency> findAll() {
        return entityManager
                .createQuery("SELECT f FROM FrequencyEntity f", FrequencyEntity.class)
                .getResultList()
                .stream()
                .map(FrequencyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Integer> findAvgHeadwaySecsByRoute() {
        List<Object[]> rows = entityManager
                .createNativeQuery(
                        "SELECT t.route_id, AVG(f.headway_secs) " +
                        "FROM frequencies f " +
                        "JOIN trips t ON f.trip_id = t.trip_id " +
                        "GROUP BY t.route_id")
                .getResultList();

        Map<String, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            String routeId = (String) row[0];
            int avgSecs = ((Number) row[1]).intValue();
            result.put(routeId, avgSecs);
        }
        return result;
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
