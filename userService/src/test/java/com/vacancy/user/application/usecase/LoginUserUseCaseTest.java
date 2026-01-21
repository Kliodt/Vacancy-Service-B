package com.vacancy.user.application.usecase;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserEventPort;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private UserEventPort userEventPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUserUseCase loginUserUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("TestUser");
        testUser.setPassword("encoded_password");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(userPersistencePort.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(userEventPort.publishUserLoggedIn(1L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("test@example.com", "password123"))
                .expectNextMatches(user -> user.getId() == 1L && user.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_UserNotFound() {
        // Arrange
        when(userPersistencePort.findByEmail("notfound@example.com")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("notfound@example.com", "password123"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_InvalidPassword() {
        // Arrange
        when(userPersistencePort.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("test@example.com", "wrongpassword"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }
}
