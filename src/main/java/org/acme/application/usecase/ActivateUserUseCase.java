package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.exception.UserAlreadyActiveException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;

import java.util.logging.Logger;

@ApplicationScoped
public class ActivateUserUseCase {

    private static final Logger log = Logger.getLogger(ActivateUserUseCase.class.getName());

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;

    @Inject
    public ActivateUserUseCase(UserRepository userRepository,
                               FirebaseUserCreator firebaseUserCreator) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
    }

    public User execute(Long id) {
        log.info("Activating user id: " + id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UserAlreadyActiveException("El usuario ya está activo");
        }

        try {
            firebaseUserCreator.enableUser(user.getFirebaseUuid());
        } catch (Exception e) {
            log.warning("Error enabling user in Firebase: " + e.getMessage());
            throw new RuntimeException("Error al activar usuario en Firebase", e);
        }

        user.setStatus(UserStatus.ACTIVE);
        return userRepository.update(user);
    }
}
