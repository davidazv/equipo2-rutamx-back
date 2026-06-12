package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Calendar;
import org.acme.domain.repository.CalendarRepository;
import org.acme.infrastructure.entities.CalendarEntity;
import org.acme.infrastructure.mapper.CalendarMapper;

import java.util.List;

@ApplicationScoped
public class CalendarRepositoryImpl implements CalendarRepository {

    private final EntityManager entityManager;

    @Inject
    public CalendarRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Calendar> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CalendarEntity c ORDER BY c.serviceId", CalendarEntity.class)
                .getResultList()
                .stream()
                .map(CalendarMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM CalendarEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Calendar> items) {
        return BatchPersister.persistAll(entityManager, items, 100, CalendarMapper::toEntity);
    }
}
