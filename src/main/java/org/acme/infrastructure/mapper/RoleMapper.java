package org.acme.infrastructure.mapper;

import org.acme.domain.models.Role;
import org.acme.infrastructure.entities.RoleEntity;

public class RoleMapper {

    private RoleMapper() {}

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        Role role = new Role();
        role.setId(entity.getId());
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());
        role.setCreatedAt(entity.getCreatedAt());
        role.setUpdatedAt(entity.getUpdatedAt());
        return role;
    }

    public static RoleEntity toEntity(Role role) {
        if (role == null) return null;
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        return entity;
    }
}
