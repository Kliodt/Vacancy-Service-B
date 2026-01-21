package com.vacancy.user.application.usecase;

import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllUsersUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private GetAllUsersUseCase getAllUsersUseCase;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setNickname("User1");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setNickname("User2");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(userPersistencePort.findAll(0, 10))
                .thenReturn(reactor.core.publisher.Flux.just(user1, user2));

        // Act & Assert
        StepVerifier.create(getAllUsersUseCase.execute(0, 10).collectList())
                .expectNextMatches(list -> list.size() == 2 && list.get(0).getId() == 1L)
                .verifyComplete();
    }

    @Test
    void testExecute_Empty() {
        // Arrange
        when(userPersistencePort.findAll(0, 10))
                .thenReturn(reactor.core.publisher.Flux.empty());

        // Act & Assert
        StepVerifier.create(getAllUsersUseCase.execute(0, 10).collectList())
                .expectNextMatches(Collection::isEmpty)
                .verifyComplete();
    }
}
