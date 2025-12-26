package com.vacancy.vacancy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.vacancy.client.OrganizationClient;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.RequestTemplate;

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
    private Long orgTestDirector = 100L;

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

        // Mock beans
        when(organizationClient.getOrganizationById(anyLong(), any()))
                .thenAnswer(invocation -> {
                    Long x = invocation.getArgument(0);
                    if (x < 100L)
                        return Map.of("director", orgTestDirector);
                    Request fakeReq = Request.create(HttpMethod.GET, "/", Collections.emptyMap(), null,
                            StandardCharsets.UTF_8, new RequestTemplate());
                    throw new FeignException.NotFound("Not found", fakeReq, null, null);
                });
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
    void testDeleteVacancy() {
        Long id = testVacancy.getId();
        vacancyService.deleteVacancy(id, orgTestDirector);
        assertEquals(0, vacancyRepository.count());
    }

    @Test
    void testUpdateVacancy() {
        Vacancy updated = new Vacancy("Updated", "Updated desc");
        updated.setOrganizationId(1L);
        updated.setSalary(200000);
        updated.setCity("Kazan");

        Vacancy result = vacancyService.updateVacancy(testVacancy.getId(), updated, orgTestDirector);
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
    void testUpdateVacancy_NotExistingVacancyThrows() {
        Vacancy upd = new Vacancy("X", "Y");
        upd.setOrganizationId(1L);
        RequestException ex = assertThrows(RequestException.class, () -> {
            vacancyService.updateVacancy(999999L, upd, upd.getOrganizationId());
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.code);
        assertEquals("Вакансия не найдена", ex.getMessage());
    }

    @Test
    void testUpdateVacancy_NotExistingOrganizationThrows() {
        Vacancy upd = new Vacancy("X", "Y");
        Long vacId = testVacancy.getId();
        upd.setOrganizationId(200L);
        RequestException ex = assertThrows(RequestException.class, () -> {
            vacancyService.updateVacancy(vacId, upd, orgTestDirector);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.code);
        assertEquals("Организация не найдена", ex.getMessage());
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
    void testCreateVacancy() {
        Vacancy newVacancy = new Vacancy("QA Engineer", "Test software");
        newVacancy.setOrganizationId(2L);
        newVacancy.setSalary(80000);
        newVacancy.setCity("Saint Petersburg");

        Vacancy created = vacancyService.createVacancy(newVacancy, orgTestDirector);
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

    @Test
    void testCreateVacancy_NotExistingOrganizationThrows() {
        Vacancy newVacancy = new Vacancy("QA Engineer", "Test software");
        newVacancy.setOrganizationId(200L);
        newVacancy.setSalary(80000);
        newVacancy.setCity("Saint Petersburg");
        RequestException ex = assertThrows(
            RequestException.class, () -> vacancyService.createVacancy(newVacancy, orgTestDirector));
        assertEquals(HttpStatus.NOT_FOUND, ex.code);
    }

}
