package com.vacancy.organization.application.usecase;

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.organization.application.exception.AccessDeniedException;
import com.vacancy.organization.application.exception.ConflictException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit тесты для UpdateOrganizationUseCase
 */
@ExtendWith(MockitoExtension.class)
class UpdateOrganizationUseCaseTest {

    @Mock
    private OrganizationPersistencePort organizationPersistencePort;

    @Mock
    private GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    @InjectMocks
    private UpdateOrganizationUseCase updateOrganizationUseCase;

    private CurrentUser ownerUser;
    private CurrentUser anotherUser;
    private Organization existingOrganization;

    @BeforeEach
    void setUp() {
        Set<Role> ownerRoles = new HashSet<>();
        ownerRoles.add(Role.ORGANIZATION);
        ownerUser = new CurrentUser(1L, ownerRoles);

        Set<Role> anotherRoles = new HashSet<>();
        anotherRoles.add(Role.ORGANIZATION);
        anotherUser = new CurrentUser(2L, anotherRoles);

        existingOrganization = new Organization();
        existingOrganization.setId(1L);
        existingOrganization.setEmail("test@example.com");
        existingOrganization.setNickname("TestOrg");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        Organization updated = new Organization();
        updated.setEmail("updated@example.com");
        updated.setNickname("UpdatedOrg");

        when(getOrganizationByIdUseCase.execute(1L)).thenReturn(Mono.just(existingOrganization));
        when(organizationPersistencePort.findByEmail("updated@example.com")).thenReturn(Mono.empty());
        when(organizationPersistencePort.save(existingOrganization)).thenReturn(Mono.just(existingOrganization));

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(1L, updated, ownerUser))
                .expectNextMatches(org -> org.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void testExecute_NotOwner() {
        // Arrange
        Organization updated = new Organization();
        updated.setEmail("updated@example.com");

        when(getOrganizationByIdUseCase.execute(1L)).thenReturn(Mono.just(existingOrganization));

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(1L, updated, anotherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_NotOrganizationRole() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(Role.SUPERVISOR);
        CurrentUser supervisorUser = new CurrentUser(1L, roles);

        Organization updated = new Organization();
        updated.setEmail("updated@example.com");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(1L, updated, supervisorUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_ConflictEmail() {
        // Arrange
        Organization anotherOrg = new Organization();
        anotherOrg.setId(2L);
        anotherOrg.setEmail("another@example.com");

        Organization updated = new Organization();
        updated.setEmail("another@example.com");
        updated.setNickname("Updated");

        when(getOrganizationByIdUseCase.execute(1L)).thenReturn(Mono.just(existingOrganization));
        when(organizationPersistencePort.findByEmail("another@example.com"))
                .thenReturn(Mono.just(anotherOrg));

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(1L, updated, ownerUser))
                .expectErrorMatches(ConflictException.class::isInstance)
                .verify();
    }
}
