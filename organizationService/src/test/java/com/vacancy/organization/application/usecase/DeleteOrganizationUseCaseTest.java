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
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationEventPort;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit тесты для DeleteOrganizationUseCase
 */
@ExtendWith(MockitoExtension.class)
class DeleteOrganizationUseCaseTest {

    @Mock
    private OrganizationPersistencePort organizationPersistencePort;

    @Mock
    private OrganizationEventPort organizationEventPort;

    @Mock
    private GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    @InjectMocks
    private DeleteOrganizationUseCase deleteOrganizationUseCase;

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
        when(getOrganizationByIdUseCase.execute(1L)).thenReturn(Mono.just(existingOrganization));
        when(organizationPersistencePort.delete(existingOrganization)).thenReturn(Mono.empty());
        when(organizationEventPort.publishOrganizationDeleted(1L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(deleteOrganizationUseCase.execute(1L, ownerUser))
                .verifyComplete();
    }

    @Test
    void testExecute_NotOwner() {
        // Arrange
        when(getOrganizationByIdUseCase.execute(1L)).thenReturn(Mono.just(existingOrganization));

        // Act & Assert
        StepVerifier.create(deleteOrganizationUseCase.execute(1L, anotherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_NotOrganizationRole() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(Role.SUPERVISOR);
        CurrentUser supervisorUser = new CurrentUser(1L, roles);

        // Act & Assert
        StepVerifier.create(deleteOrganizationUseCase.execute(1L, supervisorUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }
}
