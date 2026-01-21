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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vacancy.organization.application.exception.AccessDeniedException;
import com.vacancy.organization.application.exception.ConflictException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit тесты для CreateOrganizationUseCase
 */
@ExtendWith(MockitoExtension.class)
class CreateOrganizationUseCaseTest {

    @Mock
    private OrganizationPersistencePort organizationPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateOrganizationUseCase createOrganizationUseCase;

    private CurrentUser supervisorUser;
    private CurrentUser organizationUser;
    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        Set<Role> supervisorRoles = new HashSet<>();
        supervisorRoles.add(Role.SUPERVISOR);
        supervisorUser = new CurrentUser(1L, supervisorRoles);

        Set<Role> orgRoles = new HashSet<>();
        orgRoles.add(Role.ORGANIZATION);
        organizationUser = new CurrentUser(2L, orgRoles);

        testOrganization = new Organization();
        testOrganization.setId(3L);
        testOrganization.setEmail("test@example.com");
        testOrganization.setNickname("TestOrg");
        testOrganization.setPassword("password123");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        Organization orgToCreate = new Organization();
        orgToCreate.setEmail("new@example.com");
        orgToCreate.setNickname("NewOrg");
        orgToCreate.setPassword("password");

        when(organizationPersistencePort.findByEmail("new@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(organizationPersistencePort.save(orgToCreate)).thenReturn(Mono.just(orgToCreate));

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(orgToCreate, supervisorUser))
                .expectNextMatches(org -> org.getEmail().equals("new@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_NotSupervisor() {
        // Arrange
        Organization orgToCreate = new Organization();
        orgToCreate.setEmail("new@example.com");

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(orgToCreate, organizationUser))
                .expectErrorMatches(e -> e instanceof AccessDeniedException 
                    && e.getMessage().contains("SUPERVISOR"))
                .verify();
    }

    @Test
    void testExecute_ConflictEmail() {
        // Arrange
        Organization orgToCreate = new Organization();
        orgToCreate.setEmail("existing@example.com");

        when(organizationPersistencePort.findByEmail("existing@example.com"))
                .thenReturn(Mono.just(testOrganization));

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(orgToCreate, supervisorUser))
                .expectErrorMatches(e -> e instanceof ConflictException 
                    && e.getMessage().contains("email"))
                .verify();
    }
}
