package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.dto.ResetPasswordDto;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;

import java.util.logging.Logger;

@ApplicationScoped
public class ResetPasswordUseCase {

    private static final Logger log = Logger.getLogger(ResetPasswordUseCase.class.getName());

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;

    @Inject
    public ResetPasswordUseCase(UserRepository userRepository,
                                FirebaseUserCreator firebaseUserCreator) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
    }

    public void execute(Long id, ResetPasswordDto dto) {
        log.log(java.util.logging.Level.INFO, "Resetting password for user id: {0}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        try {
            firebaseUserCreator.resetPassword(user.getFirebaseUuid(), dto.getNewPassword());
        } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, "Error resetting password in Firebase: {0}", e.getMessage());
            throw new IllegalStateException("Error al restablecer la contraseña en Firebase", e);
        }
    }
}
