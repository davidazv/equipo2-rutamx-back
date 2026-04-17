package org.acme.application.usecase;

import org.acme.application.exception.UserAlreadySuspendedException;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.acme.infrastructure.security.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuspendUserUseCaseTest {

    private UserRepository userRepository;
    private FirebaseUserCreator firebaseUserCreator;
    private AuthContext authContext;
    private SuspendUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        firebaseUserCreator = mock(FirebaseUserCreator.class);
        authContext = mock(AuthContext.class);
        useCase = new SuspendUserUseCase(userRepository, firebaseUserCreator, authContext);
    }

    @Test
    void executeShouldSuspendUserWhenUserIsActive() throws Exception {
        User adminUser = new User();
        adminUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(3L);
        targetUser.setFirebaseUuid("firebase-uid-3");
        targetUser.setStatus(UserStatus.ACTIVE);

        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(3L)).thenReturn(Optional.of(targetUser));
        when(userRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.execute(3L);

        assertEquals(UserStatus.SUSPENDED, result.getStatus());
        verify(firebaseUserCreator).disableUser("firebase-uid-3");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertEquals(UserStatus.SUSPENDED, captor.getValue().getStatus());
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
    void executeShouldThrowUserAlreadySuspendedExceptionWhenAlreadySuspended() {
        User adminUser = new User();
        adminUser.setId(1L);

        User suspendedUser = new User();
        suspendedUser.setId(4L);
        suspendedUser.setFirebaseUuid("firebase-uid-4");
        suspendedUser.setStatus(UserStatus.SUSPENDED);

        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(4L)).thenReturn(Optional.of(suspendedUser));

        assertThrows(UserAlreadySuspendedException.class, () -> useCase.execute(4L));
        verifyNoInteractions(firebaseUserCreator);
        verify(userRepository, never()).update(any());
    }

    @Test
    void executeShouldThrowIllegalArgumentExceptionWhenAdminSuspendsOwnAccount() {
        User adminUser = new User();
        adminUser.setId(1L);

        User sameUser = new User();
        sameUser.setId(1L);
        sameUser.setFirebaseUuid("firebase-uid-1");
        sameUser.setStatus(UserStatus.ACTIVE);

        when(authContext.getUser()).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sameUser));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L));
        verifyNoInteractions(firebaseUserCreator);
    }
}
