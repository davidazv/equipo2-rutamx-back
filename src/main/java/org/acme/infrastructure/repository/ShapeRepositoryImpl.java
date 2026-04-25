package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.Shape;
import org.acme.domain.repository.ShapeRepository;
import org.acme.infrastructure.entities.ShapeEntity;
import org.acme.infrastructure.mapper.ShapeMapper;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShapeRepositoryImpl implements ShapeRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public List<Shape> findAll() {
        return entityManager
                .createQuery("SELECT s FROM ShapeEntity s ORDER BY s.shapeId, s.shapePtSequence", ShapeEntity.class)
                .getResultList()
                .stream()
                .map(ShapeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM shapes").executeUpdate();
    }

    @Override
    @Transactional
    public int createAll(List<Shape> items) {
        int count = 0;
        for (Shape item : items) {
            ShapeEntity entity = ShapeMapper.toEntity(item);
            entityManager.persist(entity);
            if (++count % 500 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
