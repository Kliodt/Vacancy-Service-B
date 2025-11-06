package com.vacancy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.exceptions.RequestException;
import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.repository.OrganizationRepository;
import com.vacancy.repository.VacancyRepository;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrganizationServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    OrganizationService organizationService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    VacancyRepository vacancyRepository;

    private Organization testOrganization;
    private Vacancy testVacancy;

    @BeforeAll
    static void beforeAll() {
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
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        vacancyRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrganization = new Organization("TestOrg", "test@example.com");
        testOrganization = organizationRepository.save(testOrganization);

        testVacancy = new Vacancy("My Vacancy", "My vacancy description");
        testVacancy.setOrganization(testOrganization);
        testVacancy = vacancyRepository.save(testVacancy);
    }

    @Test
    void getAllOrganizationsTest() {
        organizationRepository.save(new Organization("Org1", "org1@example.com"));
        organizationRepository.save(new Organization("Org2", "org2@example.com"));

        Page<Organization> result = organizationService.getAllOrganizations(0, 2);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void getAllOrganizations_shouldLimitPageSize() {
        Page<Organization> result = organizationService.getAllOrganizations(0, 100);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOrganizationByIdTest() {
        Organization result = organizationService.getOrganizationById(testOrganization.getId());
        
        assertEquals(testOrganization.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getOrganizationById_shouldThrowException_whenNotFound() {
        assertThrows(RequestException.class, () -> {
            organizationService.getOrganizationById(999L);
        });
    }

    @Test
    void createOrganizationTest() {
        Organization org = new Organization("NewOrg", "new@example.com");

        Organization result = organizationService.createOrganization(org);

        assertTrue(result.getId() > 0);
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void createOrganization_shouldThrowException_whenEmailExists() {
        Organization org = new Organization("AnotherOrg", testOrganization.getEmail());

        assertThrows(RequestException.class, () -> {
            organizationService.createOrganization(org);
        });
    }

    @Test
    void updateOrganizationTest() {
        Organization org = new Organization("UpdatedOrg", "updated@example.com");

        Organization result = organizationService.updateOrganization(testOrganization.getId(), org);

        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void updateOrganization_shouldThrowException_whenNotFound() {
        Organization org = new Organization("UpdatedOrg", "updated@example.com");

        assertThrows(RequestException.class, () -> {
            organizationService.updateOrganization(999L, org);
        });
    }

    @Test
    void updateOrganization_shouldThrowException_whenEmailExistsForOtherOrg() {
        Organization anotherOrg = new Organization("AnotherOrg", "another@example.com");
        organizationRepository.save(anotherOrg);

        Organization org = new Organization("UpdatedOrg", "another@example.com");
        Long id = testOrganization.getId();

        assertThrows(RequestException.class, () -> {
            organizationService.updateOrganization(id, org);
        });
    }

    @Test
    void deleteOrganizationTest() {
        Long orgId = testOrganization.getId();
        
        organizationService.deleteOrganization(orgId);

        assertFalse(organizationRepository.existsById(orgId));
    }

    @Test
    void getOrganizationVacanciesTest() {
        List<Vacancy> result = organizationService.getOrganizationVacancies(testOrganization.getId());

        assertEquals(1, result.size());
        assertEquals(testVacancy.getId(), result.get(0).getId());
    }

    @Test
    void publishVacancyTest() {
        Vacancy vacancy = new Vacancy("Python Developer", "Python Developer position");

        Vacancy result = organizationService.publishVacancy(testOrganization.getId(), vacancy);

        assertTrue(result.getId() > 0);
        assertEquals("Python Developer", result.getDescription());
        assertEquals(testOrganization.getId(), result.getOrganization().getId());
    }

    @Test
    void updateOrganizationVacancyTest() {
        Vacancy vacancy = new Vacancy("Updated Position", "Updated description");

        Vacancy result = organizationService.updateOrganizationVacancy(
            testOrganization.getId(), testVacancy.getId(), vacancy);

        assertEquals("Updated Position", result.getDescription());
        assertEquals("Updated description", result.getLongDescription());
    }

    @Test
    void updateOrganizationVacancy_shouldThrowException_whenVacancyNotFound() {
        Vacancy vacancy = new Vacancy("Updated Position", "Updated description");
        Long id = testOrganization.getId();

        assertThrows(RequestException.class, () -> {
            organizationService.updateOrganizationVacancy(id, 999L, vacancy);
        });
    }

    @Test
    void updateOrganizationVacancy_shouldThrowException_whenVacancyNotBelongsToOrg() {
        Organization anotherOrg = new Organization("AnotherOrg", "another@example.com");
        Organization savedAnotherOrg = organizationRepository.save(anotherOrg);

        Vacancy vacancy = new Vacancy("Updated Position", "Updated description");
        Long orgId = savedAnotherOrg.getId();
        Long vacancyId = savedAnotherOrg.getId();

        assertThrows(RequestException.class, () -> {
            organizationService.updateOrganizationVacancy(orgId, vacancyId, vacancy);
        });
    }

    @Test
    void deleteOrganizationVacancyTest() {
        Long vacancyId = testVacancy.getId();
        
        organizationService.deleteOrganizationVacancy(testOrganization.getId(), vacancyId);

        assertFalse(vacancyRepository.existsById(vacancyId));
    }

    @Test
    void deleteOrganizationVacancy_shouldThrowException_whenVacancyNotFound() {
        Long id = testOrganization.getId();
        assertThrows(RequestException.class, () -> {
            organizationService.deleteOrganizationVacancy(id, 999L);
        });
    }

    @Test
    void deleteOrganizationVacancy_shouldThrowException_whenVacancyNotBelongsToOrg() {
        Organization anotherOrg = new Organization("AnotherOrg", "another@example.com");
        Organization savedAnotherOrg = organizationRepository.save(anotherOrg);

        Long orgId = savedAnotherOrg.getId();
        Long vacancyId = savedAnotherOrg.getId();

        assertThrows(RequestException.class, () -> {
            organizationService.deleteOrganizationVacancy(orgId, vacancyId);
        });
    }
}