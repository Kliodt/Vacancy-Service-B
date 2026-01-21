package com.vacancy.user.application.usecase;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserEventPort;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private UserEventPort userEventPort;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    private CurrentUser ownerUser;
    private CurrentUser otherUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        ownerUser = new CurrentUser(1L, userRoles);
        otherUser = new CurrentUser(2L, userRoles);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("TestUser");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));
        when(userPersistencePort.delete(testUser)).thenReturn(Mono.empty());
        when(userEventPort.publishUserDeleted(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(deleteUserUseCase.execute(1L, ownerUser))
                .verifyComplete();
    }

    @Test
    void testExecute_AccessDenied() {
        // Arrange
        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(deleteUserUseCase.execute(1L, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }
}
