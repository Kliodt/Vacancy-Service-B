package com.vacancy.vacancy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.vacancy.application.usecase.CreateVacancyUseCase;
import com.vacancy.vacancy.application.usecase.DeleteVacancyUseCase;
import com.vacancy.vacancy.application.usecase.GetAllVacanciesUseCase;
import com.vacancy.vacancy.application.usecase.GetVacanciesByOrganizationUseCase;
import com.vacancy.vacancy.application.usecase.GetVacancyByIdUseCase;
import com.vacancy.vacancy.application.usecase.UpdateVacancyUseCase;
import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.EntityNotFoundException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyEventPort;
import com.vacancy.vacancy.infrastructure.persistance.repository.UserVacancyResponseJpaRepository;
import com.vacancy.vacancy.infrastructure.persistance.repository.VacancyJpaRepository;

/**
 * Integration тесты для Vacancy Use Cases
 * Тестируют взаимодействие use cases с БД через порты
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class VacancyUseCaseIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyJpaRepository vacancyRepository;

    @Autowired
    UserVacancyResponseJpaRepository responseRepository;

    // Use Cases
    @Autowired
    GetAllVacanciesUseCase getAllVacanciesUseCase;

    @Autowired
    GetVacancyByIdUseCase getVacancyByIdUseCase;

    @Autowired
    GetVacanciesByOrganizationUseCase getVacanciesByOrganizationUseCase;

    @Autowired
    CreateVacancyUseCase createVacancyUseCase;

    @Autowired
    UpdateVacancyUseCase updateVacancyUseCase;

    @Autowired
    DeleteVacancyUseCase deleteVacancyUseCase;

    @MockitoBean
    VacancyEventPort vacancyEventPort;

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
        responseRepository.deleteAll();
        vacancyRepository.deleteAll();

        Vacancy vac = new Vacancy("Java Developer", "Develop Java applications");
        vac.setOrganizationId(1L);
        vac.setSalary(100000);
        vac.setCity("Moscow");
        testVacancy = vacancyRepository.save(vac);
        assertNotNull(testVacancy);
    }

    @Test
    void testGetAllVacancies() {
        // Arrange
        Vacancy another = new Vacancy("DevOps", "CI/CD");
        another.setOrganizationId(1L);
        another.setSalary(120000);
        another.setCity("Moscow");
        vacancyRepository.save(another);

        // Act
        Page<Vacancy> page = getAllVacanciesUseCase.execute(0, 10);

        // Assert
        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testGetVacancyById_Success() {
        // Act
        Vacancy found = getVacancyByIdUseCase.execute(testVacancy.getId());

        // Assert
        assertNotNull(found);
        assertEquals(testVacancy.getId(), found.getId());
        assertEquals("Java Developer", found.getTitle());
    }

    @Test
    void testGetVacancyById_NotFound() {
        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> getVacancyByIdUseCase.execute(99999L));
        assertEquals("Вакансия не найдена", ex.getMessage());
    }

    @Test
    void testCreateVacancy_Success() {
        // Arrange
        Long orgId = 23L;
        CurrentUser orgUser = new CurrentUser(orgId, Set.of(Role.ORGANIZATION));
        Vacancy newVacancy = new Vacancy("QA Engineer", "Test software");
        newVacancy.setSalary(80000);
        newVacancy.setCity("Saint Petersburg");

        // Act
        Vacancy created = createVacancyUseCase.execute(newVacancy, orgUser);

        // Assert
        assertNotNull(created);
        assertEquals("QA Engineer", created.getTitle());
        assertEquals(orgId, created.getOrganizationId());

        Vacancy fromDb = vacancyRepository.findById(created.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("QA Engineer", fromDb.getTitle());
    }

    @Test
    void testUpdateVacancy_Success() {
        // Arrange
        CurrentUser orgUser = new CurrentUser(1L, Set.of(Role.ORGANIZATION));
        Vacancy updated = new Vacancy("Updated", "Updated desc");
        updated.setSalary(200000);
        updated.setCity("Kazan");

        // Act
        Vacancy result = updateVacancyUseCase.execute(testVacancy.getId(), updated, orgUser);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
        assertEquals("Updated desc", result.getDescription());
        assertEquals(Integer.valueOf(200000), result.getSalary());
        assertEquals("Kazan", result.getCity());
    }

    @Test
    void testUpdateVacancy_AccessDenied() {
        // Arrange
        CurrentUser differentOrg = new CurrentUser(999L, Set.of(Role.ORGANIZATION));
        Vacancy updated = new Vacancy("X", "Y");

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> updateVacancyUseCase.execute(testVacancy.getId(), updated, differentOrg));
        assertEquals("Нельзя изменять вакансии другой организации", ex.getMessage());
    }

    @Test
    void testDeleteVacancy_Success() {
        // Arrange
        Long id = testVacancy.getId();
        CurrentUser orgUser = new CurrentUser(1L, Set.of(Role.ORGANIZATION));

        // Act
        deleteVacancyUseCase.execute(id, orgUser);

        // Assert
        assertEquals(0, vacancyRepository.count());
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(vacancyEventPort, times(1)).publishVacancyDeleted(captor.capture());
        assertEquals(id, captor.getValue());
    }

    @Test
    void testDeleteVacancy_AccessDenied() {
        // Arrange
        CurrentUser differentOrg = new CurrentUser(999L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> deleteVacancyUseCase.execute(testVacancy.getId(), differentOrg));
        assertEquals("Нельзя удалять вакансии другой организации", ex.getMessage());
    }

    @Test
    void testGetVacanciesByOrganization() {
        // Arrange
        Vacancy another = new Vacancy("QA", "desc");
        another.setOrganizationId(1L);
        another.setSalary(90000);
        another.setCity("Moscow");
        vacancyRepository.save(another);

        // Act
        var list = getVacanciesByOrganizationUseCase.execute(1L);

        // Assert
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void testGetVacanciesByOrganization_Empty() {
        // Act
        var list = getVacanciesByOrganizationUseCase.execute(9999L);

        // Assert
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetAllVacancies_SizeLimit() {
        // Arrange - create many vacancies
        for (int i = 0; i < 60; i++) {
            Vacancy v = new Vacancy("Bulk" + i, "desc");
            v.setOrganizationId(2L);
            v.setSalary(50000 + i);
            v.setCity("City");
            vacancyRepository.save(v);
        }

        // Act
        Page<Vacancy> page = getAllVacanciesUseCase.execute(0, 100);

        // Assert
        assertNotNull(page);
        assertTrue(page.getSize() <= 50);
    }
}
