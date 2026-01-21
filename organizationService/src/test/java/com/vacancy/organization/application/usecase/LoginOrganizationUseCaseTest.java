package com.vacancy.organization.application.usecase;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationEventPort;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit тесты для LoginOrganizationUseCase
 */
@ExtendWith(MockitoExtension.class)
class LoginOrganizationUseCaseTest {

    @Mock
    private OrganizationPersistencePort organizationPersistencePort;

    @Mock
    private OrganizationEventPort organizationEventPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginOrganizationUseCase loginOrganizationUseCase;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        testOrganization = new Organization();
        testOrganization.setId(1L);
        testOrganization.setEmail("test@example.com");
        testOrganization.setNickname("TestOrg");
        testOrganization.setPassword("encoded_password");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(organizationPersistencePort.findByEmail("test@example.com"))
                .thenReturn(Mono.just(testOrganization));
        when(organizationEventPort.publishOrganizationLoggedIn(1L))
                .thenReturn(Mono.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("test@example.com", "password123"))
                .expectNextMatches(org -> org.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_NotFound() {
        // Arrange
        when(organizationPersistencePort.findByEmail("nonexistent@example.com"))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("nonexistent@example.com", "password"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testExecute_InvalidPassword() {
        // Arrange
        when(organizationPersistencePort.findByEmail("test@example.com"))
                .thenReturn(Mono.just(testOrganization));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("test@example.com", "wrongpassword"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }
}
