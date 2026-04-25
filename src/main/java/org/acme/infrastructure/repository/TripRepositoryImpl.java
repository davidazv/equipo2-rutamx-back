package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Trip;
import org.acme.domain.repository.TripRepository;
import org.acme.infrastructure.entities.CalendarEntity;
import org.acme.infrastructure.entities.RouteEntity;
import org.acme.infrastructure.entities.TripEntity;
import org.acme.infrastructure.mapper.TripMapper;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TripRepositoryImpl implements TripRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Trip> findAll() {
        return entityManager
                .createQuery("SELECT t FROM TripEntity t", TripEntity.class)
                .getResultList()
                .stream()
                .map(TripMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM TripEntity").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Trip> items) {
        int count = 0;
        for (Trip item : items) {
            TripEntity entity = TripMapper.toEntity(item);
            RouteEntity route = entityManager.getReference(RouteEntity.class, item.getRouteId());
            entity.setRoute(route);
            CalendarEntity calendar = entityManager.getReference(CalendarEntity.class, item.getServiceId());
            entity.setCalendar(calendar);
            entityManager.persist(entity);
            if (++count % 200 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
