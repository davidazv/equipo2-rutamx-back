package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.exception.UserAlreadySuspendedException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.acme.infrastructure.security.AuthContext;

import java.util.logging.Logger;

@ApplicationScoped
public class SuspendUserUseCase {

    private static final Logger log = Logger.getLogger(SuspendUserUseCase.class.getName());

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;
    private final AuthContext authContext;

    @Inject
    public SuspendUserUseCase(UserRepository userRepository,
                              FirebaseUserCreator firebaseUserCreator,
                              AuthContext authContext) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
        this.authContext = authContext;
    }

    public User execute(Long id) {
        log.log(java.util.logging.Level.INFO, "Suspending user id: {0}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        User caller = authContext.getUser();
        if (caller != null && caller.getId().equals(id)) {
            throw new IllegalArgumentException("No puedes suspender tu propia cuenta");
        }

        if (UserStatus.SUSPENDED.equals(user.getStatus())) {
            throw new UserAlreadySuspendedException("El usuario ya está suspendido");
        }

        try {
            firebaseUserCreator.disableUser(user.getFirebaseUuid());
        } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, "Error disabling user in Firebase: {0}", e.getMessage());
            throw new IllegalStateException("Error al suspender usuario en Firebase", e);
        }

        user.setStatus(UserStatus.SUSPENDED);
        return userRepository.update(user);
    }
}
