package org.acme.application.usecase;

import org.acme.domain.models.User;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportUsersUseCaseTest {

    private UserRepository userRepository;
    private ExportUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new ExportUsersUseCase(userRepository);
    }

    @Test
    void executeShouldReturnCsvWithHeader() {
        when(userRepository.findAll()).thenReturn(List.of());
        String csv = useCase.execute();
        assertTrue(csv.startsWith("nombre,correo,rol\n"));
    }

    @Test
    void executeShouldIncludeUserRow() {
        User user = new User();
        user.setFirstName("Ana");
        user.setLastName("García");
        user.setEmail("ana@test.com");
        user.setRoleName("CEO");
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findAll()).thenReturn(List.of(user));

        String csv = useCase.execute();
        assertTrue(csv.contains("Ana García,ana@test.com,CEO"));
    }

    @Test
    void executeShouldEscapeCommasInName() {
        User user = new User();
        user.setFirstName("Juan, Jr.");
        user.setLastName("Perez");
        user.setEmail("juan@test.com");
        user.setRoleName("ADMIN");
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findAll()).thenReturn(List.of(user));

        String csv = useCase.execute();
        assertTrue(csv.contains("\"Juan, Jr. Perez\""));
    }

    @Test
    void executeShouldHandleMultipleUsers() {
        User u1 = new User();
        u1.setFirstName("Ana");
        u1.setLastName("Lopez");
        u1.setEmail("ana@test.com");
        u1.setRoleName("CMO");
        u1.setStatus(UserStatus.ACTIVE);

        User u2 = new User();
        u2.setFirstName("Pedro");
        u2.setLastName("Soto");
        u2.setEmail("pedro@test.com");
        u2.setRoleName("COO");
        u2.setStatus(UserStatus.ACTIVE);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        String csv = useCase.execute();
        String[] lines = csv.split("\n");
        assertEquals(3, lines.length);
        assertEquals("nombre,correo,rol", lines[0]);
    }
}
