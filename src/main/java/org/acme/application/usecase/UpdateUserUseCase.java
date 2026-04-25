package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.dto.UpdateUserDto;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.repository.UserRepository;

import java.util.logging.Logger;

@ApplicationScoped
public class UpdateUserUseCase {

    private static final Logger log = Logger.getLogger(UpdateUserUseCase.class.getName());

    private final UserRepository userRepository;

    @Inject
    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(Long id, UpdateUserDto dto) {
        log.info("Updating user id: " + id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getRoleId() != null) {
            user.setRoleId(dto.getRoleId());
        }

        return userRepository.update(user);
    }
}
