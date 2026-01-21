package com.vacancy.organization.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.organization.application.exception.AccessDeniedException;
import com.vacancy.organization.application.exception.ConflictException;
import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.application.usecase.CreateOrganizationUseCase;
import com.vacancy.organization.application.usecase.DeleteOrganizationUseCase;
import com.vacancy.organization.application.usecase.GetAllOrganizationsUseCase;
import com.vacancy.organization.application.usecase.GetOrganizationByIdUseCase;
import com.vacancy.organization.application.usecase.LoginOrganizationUseCase;
import com.vacancy.organization.application.usecase.UpdateOrganizationUseCase;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationEventPort;
import com.vacancy.organization.infrastructure.persistance.repository.OrganizationR2dbcRepository;

import reactor.test.StepVerifier;

/**
 * Integration тесты для Organization Use Cases
 * Тестируют взаимодействие use cases с БД через порты
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class OrganizationUseCaseIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    OrganizationR2dbcRepository organizationRepository;

    // Use Cases
    @Autowired
    GetAllOrganizationsUseCase getAllOrganizationsUseCase;

    @Autowired
    GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    @Autowired
    CreateOrganizationUseCase createOrganizationUseCase;

    @Autowired
    UpdateOrganizationUseCase updateOrganizationUseCase;

    @Autowired
    DeleteOrganizationUseCase deleteOrganizationUseCase;

    @Autowired
    LoginOrganizationUseCase loginOrganizationUseCase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    OrganizationEventPort organizationEventPort;

    private Organization testOrganization;
    private CurrentUser supervisorUser;
    private CurrentUser organizationUser;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("api.version", "1.44");
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(), postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "fjqewh3oi4jgfng3u498gvn289rnv934h8fncv3p4fjn32vj3n8492");
        registry.add("jwt.expiration-ms", () -> 3600000);
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        organizationRepository.deleteAll().block();

        // Create test organization
        Organization org = new Organization();
        org.setEmail("test@example.com");
        org.setNickname("TestOrg");
        org.setPassword(passwordEncoder.encode("password123"));
        testOrganization = organizationRepository.save(org).block();
        assertNotNull(testOrganization);

        // Create test users
        supervisorUser = new CurrentUser(1L, Set.of(Role.SUPERVISOR));
        organizationUser = new CurrentUser(testOrganization.getId(), Set.of(Role.ORGANIZATION));

        // Mock event port
        when(organizationEventPort.publishOrganizationDeleted(anyLong())).thenReturn(reactor.core.publisher.Mono.empty());
        when(organizationEventPort.publishOrganizationLoggedIn(anyLong())).thenReturn(reactor.core.publisher.Mono.empty());
    }

    @Test
    void testGetAllOrganizations() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            Organization org = new Organization();
            org.setEmail("org" + i + "@example.com");
            org.setNickname("Org" + i);
            org.setPassword(passwordEncoder.encode("pass"));
            organizationRepository.save(org).block();
        }

        // Act & Assert
        StepVerifier.create(getAllOrganizationsUseCase.execute(0, 10).collectList())
                .expectNextMatches(list -> list.size() >= 5)
                .verifyComplete();
    }

    @Test
    void testGetAllOrganizations_paged() {
        // Arrange
        for (int i = 0; i < 60; i++) {
            Organization org = new Organization();
            org.setEmail("org" + i + "@example.com");
            org.setNickname("Org" + i);
            org.setPassword(passwordEncoder.encode("pass"));
            organizationRepository.save(org).block();
        }

        // Act & Assert - first page
        StepVerifier.create(getAllOrganizationsUseCase.execute(0, 50).collectList())
                .expectNextMatches(list -> list.size() == 50)
                .verifyComplete();

        // Act & Assert - second page
        StepVerifier.create(getAllOrganizationsUseCase.execute(1, 50).collectList())
                .expectNextMatches(list -> list.size() > 0)
                .verifyComplete();
    }

    @Test
    void testGetOrganizationById_found() {
        // Act & Assert
        StepVerifier.create(getOrganizationByIdUseCase.execute(testOrganization.getId()))
                .expectNextMatches(org -> org.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testGetOrganizationById_notFound() {
        // Act & Assert
        StepVerifier.create(getOrganizationByIdUseCase.execute(999999L))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testCreateOrganization_success() {
        // Arrange
        Organization toCreate = new Organization();
        toCreate.setEmail("new@example.com");
        toCreate.setNickname("NewOrg");
        toCreate.setPassword("password");

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(toCreate, supervisorUser))
                .expectNextMatches(org -> org.getId() != null && org.getEmail().equals("new@example.com"))
                .verifyComplete();
    }

    @Test
    void testCreateOrganization_conflict() {
        // Arrange
        Organization toCreate = new Organization();
        toCreate.setEmail(testOrganization.getEmail());
        toCreate.setNickname("NewOrg");

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(toCreate, supervisorUser))
                .expectErrorMatches(ConflictException.class::isInstance)
                .verify();
    }

    @Test
    void testCreateOrganization_notSupervisor() {
        // Arrange
        Organization toCreate = new Organization();
        toCreate.setEmail("new@example.com");

        // Act & Assert
        StepVerifier.create(createOrganizationUseCase.execute(toCreate, organizationUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testUpdateOrganization_success() {
        // Arrange
        Organization updated = new Organization();
        updated.setEmail("updated@example.com");
        updated.setNickname("Updated");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(testOrganization.getId(), updated, organizationUser))
                .expectNextMatches(org -> org.getEmail().equals("updated@example.com") 
                    && org.getNickname().equals("Updated"))
                .verifyComplete();
    }

    @Test
    void testUpdateOrganization_successSameEmail() {
        // Arrange
        Organization updated = new Organization();
        updated.setEmail(testOrganization.getEmail());
        updated.setNickname("Updated");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(testOrganization.getId(), updated, organizationUser))
                .expectNextMatches(org -> org.getEmail().equals(testOrganization.getEmail()) 
                    && org.getNickname().equals("Updated"))
                .verifyComplete();
    }

    @Test
    void testUpdateOrganization_conflictEmail() {
        // Arrange
        Organization anotherOrg = new Organization();
        anotherOrg.setEmail("another@example.com");
        anotherOrg.setNickname("AnotherOrg");
        anotherOrg = organizationRepository.save(anotherOrg).block();

        Organization updated = new Organization();
        updated.setEmail(anotherOrg.getEmail());
        updated.setNickname("Updated");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(testOrganization.getId(), updated, organizationUser))
                .expectErrorMatches(ConflictException.class::isInstance)
                .verify();
    }

    @Test
    void testUpdateOrganization_notFound() {
        // Arrange
        Organization updated = new Organization();
        updated.setEmail("x@example.com");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(999999L, updated, organizationUser))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testUpdateOrganization_notOwner() {
        // Arrange
        CurrentUser anotherUser = new CurrentUser(999L, Set.of(Role.ORGANIZATION));
        Organization updated = new Organization();
        updated.setEmail("updated@example.com");

        // Act & Assert
        StepVerifier.create(updateOrganizationUseCase.execute(testOrganization.getId(), updated, anotherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testDeleteOrganization_success() {
        // Act & Assert
        StepVerifier.create(deleteOrganizationUseCase.execute(testOrganization.getId(), organizationUser))
                .verifyComplete();

        // Verify deletion
        StepVerifier.create(getOrganizationByIdUseCase.execute(testOrganization.getId()))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testDeleteOrganization_notOwner() {
        // Arrange
        CurrentUser anotherUser = new CurrentUser(999L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        StepVerifier.create(deleteOrganizationUseCase.execute(testOrganization.getId(), anotherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testLoginOrganization_success() {
        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("test@example.com", "password123"))
                .expectNextMatches(org -> org.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void testLoginOrganization_notFound() {
        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("nonexistent@example.com", "password"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testLoginOrganization_invalidPassword() {
        // Act & Assert
        StepVerifier.create(loginOrganizationUseCase.execute("test@example.com", "wrongpassword"))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }
}
