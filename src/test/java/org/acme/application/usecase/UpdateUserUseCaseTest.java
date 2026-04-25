package org.acme.application.usecase;

import org.acme.application.dto.UpdateUserDto;
import org.acme.application.exception.UserNotFoundException;
import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserUseCaseTest {

    private UserRepository userRepository;
    private UpdateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new UpdateUserUseCase(userRepository);
    }

    @Test
    void executeShouldUpdateNonNullFieldsWhenUserExists() {
        User existing = new User();
        existing.setId(5L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setRoleId(1L);
        existing.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

        User updated = new User();
        updated.setId(5L);
        updated.setFirstName("New");
        updated.setLastName("Name");
        when(userRepository.update(any(User.class))).thenReturn(updated);

        UpdateUserDto dto = new UpdateUserDto();
        dto.setFirstName("New");

        User result = useCase.execute(5L, dto);

        assertEquals("New", result.getFirstName());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertEquals("New", captor.getValue().getFirstName());
        assertEquals("Name", captor.getValue().getLastName());
    }

    @Test
    void executeShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateUserDto dto = new UpdateUserDto();
        dto.setFirstName("Ghost");

        assertThrows(UserNotFoundException.class, () -> useCase.execute(99L, dto));
        verify(userRepository, never()).update(any());
    }

    @Test
    void executeShouldNotOverwriteNullFields() {
        User existing = new User();
        existing.setId(7L);
        existing.setFirstName("Keep");
        existing.setLastName("This");
        existing.setRoleId(2L);
        existing.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserDto dto = new UpdateUserDto();
        dto.setLastName("Updated");

        useCase.execute(7L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertEquals("Keep", captor.getValue().getFirstName());
        assertEquals("Updated", captor.getValue().getLastName());
    }
}
