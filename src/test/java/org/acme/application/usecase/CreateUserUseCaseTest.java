package org.acme.application.usecase;

import org.acme.application.dto.CreateUserDto;
import org.acme.application.exception.DuplicateEmailException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreateUserUseCaseTest {

    private UserRepository userRepository;
    private FirebaseUserCreator firebaseUserCreator;
    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        firebaseUserCreator = mock(FirebaseUserCreator.class);
        useCase = new CreateUserUseCase(userRepository, firebaseUserCreator);
    }

    @Test
    void executeShouldCreateUserWhenInputIsValid() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setFirstName("Juan");
        dto.setLastName("Perez");
        dto.setEmail("juan@test.com");
        dto.setPassword("Password1");
        dto.setRoleId(1L);

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(firebaseUserCreator.create("juan@test.com", "Password1")).thenReturn("firebase-uid-abc");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("juan@test.com");
        savedUser.setFirebaseUuid("firebase-uid-abc");
        savedUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.create(any(User.class))).thenReturn(savedUser);

        User result = useCase.execute(dto);

        assertEquals(10L, result.getId());
        assertEquals("juan@test.com", result.getEmail());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).create(captor.capture());
        assertEquals("firebase-uid-abc", captor.getValue().getFirebaseUuid());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(1L, captor.getValue().getRoleId());
    }

    @Test
    void executeShouldThrowDuplicateEmailExceptionWhenEmailExists() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("existing@test.com");
        dto.setPassword("Password1");
        dto.setRoleId(1L);
        dto.setFirstName("A");
        dto.setLastName("B");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> useCase.execute(dto));
        verifyNoInteractions(firebaseUserCreator);
    }

    @Test
    void executeShouldThrowDuplicateEmailExceptionWhenFirebaseReportsEmailExists() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("fb.dup@test.com");
        dto.setPassword("Password1");
        dto.setRoleId(1L);
        dto.setFirstName("A");
        dto.setLastName("B");

        when(userRepository.existsByEmail("fb.dup@test.com")).thenReturn(false);
        when(firebaseUserCreator.create(anyString(), anyString()))
                .thenThrow(new RuntimeException("EMAIL_ALREADY_EXISTS"));

        assertThrows(DuplicateEmailException.class, () -> useCase.execute(dto));
        verify(userRepository, never()).create(any());
    }
}
