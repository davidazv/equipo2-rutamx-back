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
import java.util.stream.Collectors;

@ApplicationScoped
public class CalendarRepositoryImpl implements CalendarRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Calendar> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CalendarEntity c ORDER BY c.serviceId", CalendarEntity.class)
                .getResultList()
                .stream()
                .map(CalendarMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM CalendarEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Calendar> items) {
        int count = 0;
        for (Calendar item : items) {
            CalendarEntity entity = CalendarMapper.toEntity(item);
            entityManager.persist(entity);
            if (++count % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
