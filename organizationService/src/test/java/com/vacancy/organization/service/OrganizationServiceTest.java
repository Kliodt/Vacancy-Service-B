package com.vacancy.organization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.organization.exceptions.RequestException;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
class OrganizationServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    OrganizationService organizationService;

    private Organization testOrganization;
    private Long testOrganizationDirector = 10L;

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
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        organizationRepository.deleteAll().block();

        // Create test vacancy
        Organization org = new Organization();
        org.setEmail("example@gmail.com");
        org.setNickname("TestOrg");
        org.setDirector(testOrganizationDirector);
        // Save a test organization for use in tests
        testOrganization = organizationRepository.save(org).block();
        assertNotNull(testOrganization);
    }

    @Test
    void getOrganizationById_found() {
        StepVerifier.create(organizationService.getOrganizationById(testOrganization.getId()))
                .expectNextMatches(o -> o.getEmail().equals(testOrganization.getEmail()))
                .verifyComplete();
    }

    @Test
    void getOrganizationById_notFound() {
        StepVerifier.create(organizationService.getOrganizationById(999999L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void createOrganization_success() {
        Organization toCreate = new Organization();
        toCreate.setEmail("new@example.com");
        toCreate.setNickname("NewOrg");

        StepVerifier.create(organizationService.createOrganization(toCreate, 1L))
                .expectNextMatches(o -> o.getId() != null && o.getEmail().equals("new@example.com"))
                .verifyComplete();
    }

    @Test
    void createOrganization_conflict() {
        Organization toCreate = new Organization();
        toCreate.setEmail(testOrganization.getEmail());
        toCreate.setNickname("NewOrg");

        StepVerifier.create(organizationService.createOrganization(toCreate, 1L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.CONFLICT, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void updateOrganization_notFound() {
        Organization upd = new Organization();
        upd.setEmail("x@example.com");
        upd.setNickname("X");

        StepVerifier.create(organizationService.updateOrganization(999999L, upd, 1L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void updateOrganization_conflictEmail() {
        Organization newOrg = new Organization();
        newOrg.setEmail("new@example.com");
        newOrg.setNickname("NewOrg");
        newOrg = organizationRepository.save(newOrg).block();
        assertNotNull(newOrg);

        // try to set email of 'testOrganization' when updating 'newOrganization'
        Organization upd = new Organization();
        upd.setEmail(testOrganization.getEmail());
        upd.setNickname("Updated");

        StepVerifier.create(organizationService.updateOrganization(newOrg.getId(), upd, 1L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.CONFLICT, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void updateOrganization_success() {
        Organization upd = new Organization();
        upd.setEmail("updated@example.com");
        upd.setNickname("Updated");

        Organization saved = new Organization();
        saved.setId(testOrganization.getId());
        saved.setEmail(upd.getEmail());
        saved.setNickname(upd.getNickname());

        StepVerifier.create(organizationService.updateOrganization(testOrganization.getId(), upd, testOrganizationDirector))
                .expectNextMatches(o -> o.getEmail().equals("updated@example.com") && o.getNickname().equals("Updated"))
                .verifyComplete();
    }

    @Test
    void updateOrganization_successSameEmail() {
        Organization upd = new Organization();
        upd.setEmail(testOrganization.getEmail());
        upd.setNickname("Updated");

        StepVerifier.create(organizationService.updateOrganization(testOrganization.getId(), upd, testOrganizationDirector))
                .expectNextMatches(o -> o.getEmail().equals(testOrganization.getEmail()) && o.getNickname().equals("Updated"))
                .verifyComplete();
    }

    @Test
    void deleteOrganization_completes() {
        StepVerifier.create(organizationService.deleteOrganization(testOrganization.getId(), testOrganizationDirector)).verifyComplete();
    }

    @Test
    void getAllOrganizations() {
        for (int i = 0; i < 5; i++) {
            Organization o = new Organization();
            o.setEmail("org" + i + "@example.com");
            o.setNickname("Org" + i);
            organizationRepository.save(o).block();
        }
        Long count = organizationRepository.count().block();

        StepVerifier.create(organizationService.getAllOrganizations(0, 10).collectList())
                .expectNextMatches(list -> list.size() == count)
                .verifyComplete();
    }

    @Test
    void getAllOrganizations_paged() {
        for (int i = 0; i < 60; i++) {
            Organization o = new Organization();
            o.setEmail("org" + i + "@example.com");
            o.setNickname("Org" + i);
            organizationRepository.save(o).block();
        }
        Long count = organizationRepository.count().block();

        StepVerifier.create(organizationService.getAllOrganizations(0, 60).collectList())
                .expectNextMatches(list -> list.size() == 50)
                .verifyComplete();

        StepVerifier.create(organizationService.getAllOrganizations(1, 60).collectList())
                .expectNextMatches(list -> list.size() == count - 50)
                .verifyComplete();
    }

}
