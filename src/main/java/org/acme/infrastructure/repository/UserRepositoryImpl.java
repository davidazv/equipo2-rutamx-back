package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.domain.models.PagedResult;
import org.acme.domain.models.User;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.entities.RoleEntity;
import org.acme.infrastructure.entities.UserEntity;
import org.acme.infrastructure.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager entityManager;

    @Inject
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public User create(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        RoleEntity role = entityManager.find(RoleEntity.class, user.getRoleId());
        entity.setRole(role);
        entity.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        entityManager.persist(entity);
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        List<UserEntity> results = entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role WHERE u.id = :id",
                        UserEntity.class)
                .setParameter("id", id)
                .getResultList();
        return results.stream().findFirst().map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByFirebaseUuid(String firebaseUuid) {
        List<UserEntity> results = entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role WHERE u.firebaseUuid = :uuid",
                        UserEntity.class)
                .setParameter("uuid", firebaseUuid)
                .getResultList();
        return results.stream().findFirst().map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role ORDER BY u.id",
                        UserEntity.class)
                .getResultList()
                .stream()
                .map(UserMapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<User> findPaginated(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 200));

        long total = entityManager
                .createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
                .getSingleResult();

        List<User> items = entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role ORDER BY u.id",
                        UserEntity.class)
                .setFirstResult(safePage * safeSize)
                .setMaxResults(safeSize)
                .getResultList()
                .stream()
                .map(UserMapper::toDomain)
                .toList();

        return new PagedResult<>(items, total, safePage, safeSize);
    }

    @Override
    @Transactional
    public User update(User user) {
        List<UserEntity> results = entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role WHERE u.id = :id",
                        UserEntity.class)
                .setParameter("id", user.getId())
                .getResultList();

        UserEntity entity = results.stream().findFirst()
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "UserEntity not found: " + user.getId()));

        if (user.getFirstName() != null) entity.setFirstName(user.getFirstName());
        if (user.getLastName() != null) entity.setLastName(user.getLastName());
        if (user.getRoleId() != null
                && (entity.getRole() == null || !user.getRoleId().equals(entity.getRole().getId()))) {
            RoleEntity role = entityManager.find(RoleEntity.class, user.getRoleId());
            entity.setRole(role);
        }
        if (user.getStatus() != null) entity.setStatus(user.getStatus());
        entity.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        return UserMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity entity = entityManager.find(UserEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<UserEntity> results = entityManager
                .createQuery(
                        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.role WHERE u.email = :email",
                        UserEntity.class)
                .setParameter("email", email)
                .getResultList();
        return results.stream().findFirst().map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager
                .createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }
}
