package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.acme.infrastructure.security.AuthContext;

import java.util.logging.Logger;

@ApplicationScoped
public class DeleteUserUseCase {

    private static final Logger log = Logger.getLogger(DeleteUserUseCase.class.getName());

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;
    private final AuthContext authContext;

    @Inject
    public DeleteUserUseCase(UserRepository userRepository,
                             FirebaseUserCreator firebaseUserCreator,
                             AuthContext authContext) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
        this.authContext = authContext;
    }

    public void execute(Long id) {
        log.info("Deleting user id: " + id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        User caller = authContext.getUser();
        if (caller != null && caller.getId().equals(id)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta");
        }

        try {
            firebaseUserCreator.deleteUser(user.getFirebaseUuid());
        } catch (FirebaseAuthException e) {
            if (AuthErrorCode.USER_NOT_FOUND.equals(e.getAuthErrorCode())) {
                log.warning("User not found in Firebase, proceeding to delete from DB: " + user.getFirebaseUuid());
            } else {
                log.warning("Error deleting user from Firebase: " + e.getMessage());
                throw new RuntimeException("Error al eliminar usuario en Firebase", e);
            }
        }

        userRepository.delete(id);
    }
}
