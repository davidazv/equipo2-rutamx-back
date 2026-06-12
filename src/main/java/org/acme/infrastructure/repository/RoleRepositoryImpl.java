package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.domain.models.Role;
import org.acme.domain.repository.RoleRepository;
import org.acme.infrastructure.entities.RoleEntity;
import org.acme.infrastructure.mapper.RoleMapper;

import java.util.Optional;

@ApplicationScoped
public class RoleRepositoryImpl implements RoleRepository {

    private final EntityManager entityManager;

    @Inject
    public RoleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Role> findById(Long id) {
        RoleEntity entity = entityManager.find(RoleEntity.class, id);
        return Optional.ofNullable(entity).map(RoleMapper::toDomain);
    }
}
