package com.vacancy.organization.application.usecase;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit тесты для GetOrganizationByIdUseCase
 */
@ExtendWith(MockitoExtension.class)
class GetOrganizationByIdUseCaseTest {

    @Mock
    private OrganizationPersistencePort organizationPersistencePort;

    @InjectMocks
    private GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        testOrganization = new Organization();
        testOrganization.setId(1L);
        testOrganization.setEmail("test@example.com");
        testOrganization.setNickname("TestOrg");
        testOrganization.setPassword("password123");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(organizationPersistencePort.findById(1L)).thenReturn(Mono.just(testOrganization));

        // Act & Assert
        StepVerifier.create(getOrganizationByIdUseCase.execute(1L))
                .expectNextMatches(org -> 
                    org.getId().equals(1L) && org.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testExecute_NotFound() {
        // Arrange
        when(organizationPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(getOrganizationByIdUseCase.execute(999L))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }
}
