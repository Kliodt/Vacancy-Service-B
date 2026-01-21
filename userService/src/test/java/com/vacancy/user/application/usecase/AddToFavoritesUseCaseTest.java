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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserPersistencePort;
import com.vacancy.user.domain.port.VacancyServicePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddToFavoritesUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private VacancyServicePort vacancyServicePort;

    @InjectMocks
    private AddToFavoritesUseCase addToFavoritesUseCase;

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
        com.vacancy.user.domain.model.User user = new com.vacancy.user.domain.model.User();
        user.setId(1L);
        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(user));
        when(vacancyServicePort.getVacancyById(10L)).thenReturn(Mono.empty());
        when(userPersistencePort.addToFavorites(1L, 10L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(addToFavoritesUseCase.execute(1L, 10L, ownerUser))
                .verifyComplete();
    }

    @Test
    void testExecute_AccessDenied() {
        // Arrange
        // Act & Assert
        StepVerifier.create(addToFavoritesUseCase.execute(1L, 10L, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_VacancyNotFound() {
        // Arrange
        com.vacancy.user.domain.model.User user = new com.vacancy.user.domain.model.User();
        user.setId(1L);
        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(user));
        when(vacancyServicePort.getVacancyById(999L)).thenReturn(Mono.error(new EntityNotFoundException("Vacancy not found")));

        // Act & Assert
        StepVerifier.create(addToFavoritesUseCase.execute(1L, 999L, ownerUser))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }
}
