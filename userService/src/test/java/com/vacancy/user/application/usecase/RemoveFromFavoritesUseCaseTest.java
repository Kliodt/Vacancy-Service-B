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

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RemoveFromFavoritesUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private RemoveFromFavoritesUseCase removeFromFavoritesUseCase;

    private CurrentUser ownerUser;
    private CurrentUser otherUser;

    @BeforeEach
    void setUp() {
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        ownerUser = new CurrentUser(1L, userRoles);
        otherUser = new CurrentUser(2L, userRoles);
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(userPersistencePort.removeFromFavorites(1L, 10L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(removeFromFavoritesUseCase.execute(1L, 10L, ownerUser))
                .verifyComplete();
    }

    @Test
    void testExecute_AccessDenied() {
        // Arrange
        // Act & Assert
        StepVerifier.create(removeFromFavoritesUseCase.execute(1L, 10L, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }
}
