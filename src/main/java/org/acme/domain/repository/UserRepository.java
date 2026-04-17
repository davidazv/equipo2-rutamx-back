package org.acme.domain.repository;

import org.acme.domain.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User create(User user);
    Optional<User> findById(Long id);
    Optional<User> findByFirebaseUuid(String firebaseUuid);
    List<User> findAll();
    User update(User user);
    void delete(Long id);
    boolean existsByEmail(String email);
}
