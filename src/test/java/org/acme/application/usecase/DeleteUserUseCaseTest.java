package org.acme.application.usecase;

import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.acme.infrastructure.security.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserUseCaseTest {

    private UserRepository userRepository;
    private FirebaseUserCreator firebaseUserCreator;
    private AuthContext authContext;
    private DeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        firebaseUserCreator = mock(FirebaseUserCreator.class);
        authContext = mock(AuthContext.class);
        useCase = new DeleteUserUseCase(userRepository, firebaseUserCreator, authContext);
    }

    @Test
    void executeShouldDeleteUserWhenIdIsValid() throws Exception {
        User adminUser = new User();
        adminUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setFirebaseUuid("firebase-uid-2");
        targetUser.setStatus(UserStatus.ACTIVE);

        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        useCase.execute(2L);

        verify(firebaseUserCreator).deleteUser("firebase-uid-2");
        verify(userRepository).delete(2L);
    }

    @Test
    void executeShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        User adminUser = new User();
        adminUser.setId(1L);
        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.execute(99L));
        verifyNoInteractions(firebaseUserCreator);
    }

    @Test
    void executeShouldThrowIllegalArgumentExceptionWhenAdminDeletesOwnAccount() {
        User adminUser = new User();
        adminUser.setId(1L);

        User sameUser = new User();
        sameUser.setId(1L);
        sameUser.setFirebaseUuid("firebase-uid-1");

        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sameUser));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L));
        verifyNoInteractions(firebaseUserCreator);
        verify(userRepository, never()).delete(any());
    }
}
