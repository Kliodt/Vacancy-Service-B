package com.vacancy.user.application.usecase;

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.ConflictException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private CurrentUser supervisorUser;
    private CurrentUser regularUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        Set<Role> supervisorRoles = new HashSet<>();
        supervisorRoles.add(Role.SUPERVISOR);
        supervisorUser = new CurrentUser(1L, supervisorRoles);

        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        regularUser = new CurrentUser(2L, userRoles);

        testUser = new User();
        testUser.setId(3L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("TestUser");
        testUser.setPassword("password123");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setEmail("new@example.com");
        userToCreate.setNickname("NewUser");
        userToCreate.setPassword("password");
        userToCreate.setRoles(new HashSet<>(Set.of(Role.USER)));

        when(userPersistencePort.findByEmail("new@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userPersistencePort.save(userToCreate)).thenReturn(Mono.just(userToCreate));

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(userToCreate, supervisorUser))
                .expectNextMatches(user -> user.getEmail().equals("new@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_NotSupervisor() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setEmail("new@example.com");

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(userToCreate, regularUser))
                .expectErrorMatches(e -> e instanceof AccessDeniedException
                    && e.getMessage().contains("SUPERVISOR"))
                .verify();
    }

    @Test
    void testExecute_ConflictEmail() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setEmail("existing@example.com");

        when(userPersistencePort.findByEmail("existing@example.com"))
                .thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(userToCreate, supervisorUser))
                .expectErrorMatches(e -> e instanceof ConflictException
                    && e.getMessage().contains("email"))
                .verify();
    }
}
