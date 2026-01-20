package com.vacancy.vacancy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.kafka.KafkaProducerService;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

/*
Bugfixes:
fix 1 (test containers): https://github.com/testcontainers/testcontainers-java/issues/11212#issuecomment-3518584924
fix 2 (vscode test runner): https://github.com/microsoft/vscode-java-test/issues/1714
*/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@WithMockUser(roles = "USER")
class VacancyServiceTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;
    @Autowired
    VacancyService vacancyService;
    @MockitoBean
    KafkaProducerService kafkaProducerService;

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
        registry.add("jwt.secret", () -> "fjqewh3oi4jgfng3u498gvn289rnv934h8fncv3p4fjn32vj3n8492");
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
    }

    @Test
    void testGetAllVacancies() {
        Vacancy another = new Vacancy("DevOps", "CI/CD");
        another.setOrganizationId(1L);
        another.setSalary(120000);
        another.setCity("Moscow");
        vacancyRepository.save(another);

        var page = vacancyService.getAllVacancies(0, 10);
        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testDeleteVacancy() {
        Long id = testVacancy.getId();
        Long orgId = testVacancy.getOrganizationId();

        vacancyService.deleteVacancy(id, new TestingAuthenticationToken(orgId, null));
        assertEquals(0, vacancyRepository.count());

        // Verify Kafka producer was called with correct vacancy id
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(kafkaProducerService, times(1)).sendVacancyDeleted(captor.capture());
        assertEquals(id, captor.getValue());
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testDeleteVacancy_Forbidden() {
        Long id = testVacancy.getId();
        RequestException ex = assertThrows(RequestException.class,
                () -> vacancyService.deleteVacancy(id, new TestingAuthenticationToken(999L, null)));
        assertEquals(HttpStatus.FORBIDDEN, ex.code);
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testUpdateVacancy() {
        Vacancy updated = new Vacancy("Updated", "Updated desc");
        updated.setOrganizationId(1L);
        updated.setSalary(200000);
        updated.setCity("Kazan");

        Vacancy result = vacancyService.updateVacancy(testVacancy.getId(), updated,
                new TestingAuthenticationToken(testVacancy.getOrganizationId(), null));
        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
        assertEquals("Updated desc", result.getDescription());
        assertEquals(Integer.valueOf(200000), result.getSalary());
        assertEquals("Kazan", result.getCity());
    }

    @Test
    void testGetVacanciesByOrganization() {
        Vacancy another = new Vacancy("QA", "desc");
        another.setOrganizationId(1L);
        another.setSalary(90000);
        another.setCity("Moscow");
        vacancyRepository.save(another);

        var list = vacancyService.getVacanciesByOrganization(1L);
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void testGetAllVacancies_SizeLimitAndPageOutOfRange() {
        // create many vacancies to exceed size limit
        for (int i = 0; i < 60; i++) {
            Vacancy v = new Vacancy("Bulk" + i, "desc");
            v.setOrganizationId(2L);
            v.setSalary(50000 + i);
            v.setCity("City");
            vacancyRepository.save(v);
        }

        // request with size > 50 should be capped to 50
        var page = vacancyService.getAllVacancies(0, 100);
        assertNotNull(page);
        assertTrue(page.getSize() <= 50);

        // request page beyond last should return empty content
        var emptyPage = vacancyService.getAllVacancies(100, 10);
        assertNotNull(emptyPage);
        assertTrue(emptyPage.isEmpty());
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testUpdateVacancy_NotExistingVacancyThrows() {
        Vacancy upd = new Vacancy("X", "Y");
        upd.setOrganizationId(1L);
        RequestException ex = assertThrows(RequestException.class, () -> {
            vacancyService.updateVacancy(999999L, upd, new TestingAuthenticationToken(upd.getOrganizationId(), null));
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.code);
        assertEquals("Вакансия не найдена", ex.getMessage());
    }


    @Test
    void testGetVacanciesByOrganization_Empty() {
        var list = vacancyService.getVacanciesByOrganization(9999L);
        assertNotNull(list);
        assertTrue(list.isEmpty());
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
    @WithMockUser(roles = "ORGANIZATION")
    void testCreateVacancy() {
        Long testOrgId = 23L;
        Vacancy newVacancy = new Vacancy("QA Engineer", "Test software");
        newVacancy.setOrganizationId(testOrgId);
        newVacancy.setSalary(80000);
        newVacancy.setCity("Saint Petersburg");

        Vacancy created = vacancyService.createVacancy(newVacancy,
                new TestingAuthenticationToken(testOrgId, null));
        assertNotNull(created);
        assertEquals("QA Engineer", created.getTitle());
        assertEquals("Test software", created.getDescription());
        assertEquals(testOrgId, created.getOrganizationId());
        assertEquals(Integer.valueOf(80000), created.getSalary());
        assertEquals("Saint Petersburg", created.getCity());

        // Проверяем, что вакансия сохранена в репозитории
        Vacancy fromDb = vacancyRepository.findById(created.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("QA Engineer", fromDb.getTitle());
    }

}
