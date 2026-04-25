package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.dto.CreateUserDto;
import org.acme.application.exception.DuplicateEmailException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;

import java.util.logging.Logger;

@ApplicationScoped
public class CreateUserUseCase {

    private static final Logger log = Logger.getLogger(CreateUserUseCase.class.getName());

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;

    @Inject
    public CreateUserUseCase(UserRepository userRepository, FirebaseUserCreator firebaseUserCreator) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
    }

    public User execute(CreateUserDto dto) {
        log.info("Creating user with email: " + dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("El correo ya está registrado");
        }

        String firebaseUuid;
        try {
            firebaseUuid = firebaseUserCreator.create(dto.getEmail(), dto.getPassword());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("EMAIL_ALREADY_EXISTS")) {
                throw new DuplicateEmailException("El correo ya está registrado");
            }
            throw new RuntimeException("Error al crear usuario en Firebase", e);
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirebaseUuid(firebaseUuid);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRoleId(dto.getRoleId());
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.create(user);
    }
}
