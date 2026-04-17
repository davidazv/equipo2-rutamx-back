package org.acme.infrastructure.mapper;

import org.acme.domain.models.User;
import org.acme.infrastructure.entities.UserEntity;
import org.hibernate.Hibernate;

public class UserMapper {

    private UserMapper() {}

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setFirebaseUuid(entity.getFirebaseUuid());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setStatus(entity.getStatus());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getRole() != null && Hibernate.isInitialized(entity.getRole())) {
            user.setRoleId(entity.getRole().getId());
            user.setRoleName(entity.getRole().getName());
        }

        return user;
    }

    public static UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity entity = new UserEntity();
        entity.setEmail(user.getEmail());
        entity.setFirebaseUuid(user.getFirebaseUuid());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setStatus(user.getStatus());
        return entity;
    }
}
