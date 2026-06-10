package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Stop;
import org.acme.domain.repository.StopRepository;
import org.acme.infrastructure.entities.StopEntity;
import org.acme.infrastructure.mapper.StopMapper;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StopRepositoryImpl implements StopRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Stop> findAll() {
        return entityManager
                .createQuery("SELECT s FROM StopEntity s ORDER BY s.stopName", StopEntity.class)
                .getResultList()
                .stream()
                .map(StopMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM stops").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Stop> items) {
        return BatchPersister.persistAll(entityManager, items, 1000, StopMapper::toEntity);
    }
}
