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
import com.vacancy.user.application.exception.ConflictException;
import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.FileServicePort;
import com.vacancy.user.domain.port.UserPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private FileServicePort fileServicePort;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

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
        testUser.setPassword("password123");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setCvLink("http://cv.link");

        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));
        when(userPersistencePort.findByEmail("updated@example.com")).thenReturn(Mono.empty());
        when(fileServicePort.getFileById("http://cv.link")).thenReturn(Mono.empty());
        when(userPersistencePort.save(testUser)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(1L, updatedUser, ownerUser))
                .expectNextMatches(user -> user.getId() == 1L)
                .verifyComplete();
    }

    @Test
    void testExecute_NotFound() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");

        when(userPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(999L, updatedUser, ownerUser))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_AccessDenied() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");

        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(1L, updatedUser, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_ConflictEmail() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");
        updatedUser.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userPersistencePort.findById(1L)).thenReturn(Mono.just(testUser));
        when(userPersistencePort.findByEmail("existing@example.com")).thenReturn(Mono.just(existingUser));

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(1L, updatedUser, ownerUser))
                .expectErrorMatches(ConflictException.class::isInstance)
                .verify();
    }
}
