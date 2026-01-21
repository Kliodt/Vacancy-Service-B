package com.vacancy.user.application.usecase;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private GetUserByIdUseCase getUserByIdUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("TestUser");
        testUser.setPassword("password123");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(getUserByIdUseCase.execute(1L))
                .expectNextMatches(user -> user.getId() == 1L && user.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_NotFound() {
        // Arrange
        when(userPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(getUserByIdUseCase.execute(999L))
                .expectErrorMatches(e -> e instanceof EntityNotFoundException
                    && e.getMessage().contains("Пользователь не найден"))
                .verify();
    }
}
