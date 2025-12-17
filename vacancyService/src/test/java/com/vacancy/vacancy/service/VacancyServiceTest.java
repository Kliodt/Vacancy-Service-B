package com.vacancy.vacancy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.vacancy.client.OrganizationClient;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.cloud.config.enabled=false")
class VacancyServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;
    @Autowired
    VacancyService vacancyService;
    @MockitoBean
    private OrganizationClient organizationClient;

    private Vacancy testVacancy;

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
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        responseRepository.deleteAll();
        vacancyRepository.deleteAll();

        // Create test vacancy
        Vacancy vac = new Vacancy("Java Developer", "Develop Java applications");
        vac.setOrganizationId(1L);
        vac.setSalary(100000);
        vac.setCity("Moscow");
        testVacancy = vacancyRepository.save(vac);
        assertNotNull(testVacancy);

        // Mock beans
        when(organizationClient.getOrganizationById(anyLong())).thenReturn(new Object());
    }

    @Test
    void testGetVacancyById() {
        Vacancy found = vacancyService.getVacancyById(testVacancy.getId());
        assertNotNull(found);
        assertEquals(testVacancy.getId(), found.getId());
        assertEquals(testVacancy.getTitle(), found.getTitle());
        assertEquals(testVacancy.getDescription(), found.getDescription());
        assertEquals(testVacancy.getOrganizationId(), found.getOrganizationId());
        assertEquals(testVacancy.getSalary(), found.getSalary());
        assertEquals(testVacancy.getCity(), found.getCity());
    }

    @Test
    void createVacancyTest() {
        Vacancy newVacancy = new Vacancy("QA Engineer", "Test software");
        newVacancy.setOrganizationId(2L);
        newVacancy.setSalary(80000);
        newVacancy.setCity("Saint Petersburg");

        Vacancy created = vacancyService.createVacancy(newVacancy);
        assertNotNull(created);
        assertEquals("QA Engineer", created.getTitle());
        assertEquals("Test software", created.getDescription());
        assertEquals(Long.valueOf(2L), created.getOrganizationId());
        assertEquals(Integer.valueOf(80000), created.getSalary());
        assertEquals("Saint Petersburg", created.getCity());

        // Проверяем, что вакансия сохранена в репозитории
        Vacancy fromDb = vacancyRepository.findById(created.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("QA Engineer", fromDb.getTitle());
    }

}
