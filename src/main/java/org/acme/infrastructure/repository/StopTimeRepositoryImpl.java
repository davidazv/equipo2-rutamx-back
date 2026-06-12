package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.StopTime;
import org.acme.domain.repository.StopTimeRepository;
import org.acme.infrastructure.entities.StopEntity;
import org.acme.infrastructure.entities.StopTimeEntity;
import org.acme.infrastructure.entities.TripEntity;
import org.acme.infrastructure.mapper.StopTimeMapper;

import java.util.List;

@ApplicationScoped
public class StopTimeRepositoryImpl implements StopTimeRepository {

    private final EntityManager entityManager;

    @Inject
    public StopTimeRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM stop_times").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<StopTime> items) {
        return BatchPersister.persistAll(entityManager, items, 500, item -> {
            StopTimeEntity entity = StopTimeMapper.toEntity(item);
            TripEntity trip = entityManager.getReference(TripEntity.class, item.getTripId());
            entity.setTrip(trip);
            StopEntity stop = entityManager.getReference(StopEntity.class, item.getStopId());
            entity.setStop(stop);
            return entity;
        });
    }
}
