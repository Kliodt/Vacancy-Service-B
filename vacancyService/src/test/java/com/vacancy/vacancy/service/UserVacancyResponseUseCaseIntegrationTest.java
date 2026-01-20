package com.vacancy.vacancy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.vacancy.application.usecase.ChangeResponseStatusUseCase;
import com.vacancy.vacancy.application.usecase.GetUserResponsesUseCase;
import com.vacancy.vacancy.application.usecase.GetVacancyByIdUseCase;
import com.vacancy.vacancy.application.usecase.GetVacancyResponsesUseCase;
import com.vacancy.vacancy.application.usecase.RemoveResponseFromVacancyUseCase;
import com.vacancy.vacancy.application.usecase.RespondToVacancyUseCase;
import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyEventPort;
import com.vacancy.vacancy.infrastructure.persistance.repository.UserVacancyResponseJpaRepository;
import com.vacancy.vacancy.infrastructure.persistance.repository.VacancyJpaRepository;

/**
 * Integration тесты для User Vacancy Response Use Cases
 * Тестируют отклики на вакансии через use cases с реальной БД
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class UserVacancyResponseUseCaseIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyJpaRepository vacancyRepository;

    @Autowired
    UserVacancyResponseJpaRepository responseRepository;

    // Use Cases
    @Autowired
    RespondToVacancyUseCase respondToVacancyUseCase;

    @Autowired
    RemoveResponseFromVacancyUseCase removeResponseFromVacancyUseCase;

    @Autowired
    GetUserResponsesUseCase getUserResponsesUseCase;

    @Autowired
    GetVacancyResponsesUseCase getVacancyResponsesUseCase;

    @Autowired
    ChangeResponseStatusUseCase changeResponseStatusUseCase;

    @Autowired
    GetVacancyByIdUseCase getVacancyByIdUseCase;

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
    void testRespondToVacancy_Success() {
        // Arrange
        long userId = 5L;
        CurrentUser currentUser = new CurrentUser(userId, Set.of(Role.USER));

        // Act
        UserVacancyResponse response = respondToVacancyUseCase.execute(testVacancy.getId(), currentUser);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(testVacancy.getId(), response.getVacancyId());
        verify(vacancyEventPort, times(1)).publishVacancyResponseCreated(response);
    }

    @Test
    void testRespondToVacancy_ForbiddenRole_OrganizationRole() {
        // Arrange
        CurrentUser organizationUser = new CurrentUser(1L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> respondToVacancyUseCase.execute(testVacancy.getId(), organizationUser));
        assertEquals("Только пользователи могут откликаться на вакансии", ex.getMessage());
    }

    @Test
    void testRespondToVacancy_DontDuplicate() {
        // Arrange
        long userId = 5L;
        long vacancyId = testVacancy.getId();
        CurrentUser currentUser = new CurrentUser(userId, Set.of(Role.USER));

        assertEquals(0, responseRepository.count());

        // Act
        respondToVacancyUseCase.execute(vacancyId, currentUser);
        respondToVacancyUseCase.execute(vacancyId, currentUser);
        respondToVacancyUseCase.execute(vacancyId, currentUser);

        // Assert
        assertEquals(1, responseRepository.count());
        verify(vacancyEventPort, times(3)).publishVacancyResponseCreated(ArgumentCaptor.forClass(UserVacancyResponse.class).capture());
    }

    @Test
    void testRemoveResponseFromVacancy() {
        // Arrange
        long userId = 11L;
        long vacancyId = testVacancy.getId();
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        CurrentUser currentUser = new CurrentUser(userId, Set.of(Role.USER));
        var before = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertEquals(1, before.size());

        // Act
        removeResponseFromVacancyUseCase.execute(vacancyId, currentUser);

        // Assert
        var after = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertTrue(after == null || after.isEmpty());
    }

    @Test
    void testGetUserResponses() {
        // Arrange
        Long userA = 21L;
        Long userB = 22L;
        Long vacancyA = testVacancy.getId();

        Vacancy vac2 = new Vacancy("X", "Y");
        Vacancy savedVac2 = vacancyRepository.save(vac2);

        responseRepository.save(new UserVacancyResponse(userA, vacancyA));
        responseRepository.save(new UserVacancyResponse(userA, savedVac2.getId()));
        responseRepository.save(new UserVacancyResponse(userB, vacancyA));

        CurrentUser userAContext = new CurrentUser(userA, Set.of(Role.USER));

        // Act
        List<UserVacancyResponse> userResponses = getUserResponsesUseCase.execute(userAContext);

        // Assert
        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());
    }

    @Test
    void testGetVacancyResponses_Success() {
        // Arrange
        Long userA = 21L;
        Long userB = 22L;
        Long vacancyA = testVacancy.getId();
        Long orgA = testVacancy.getOrganizationId();

        Vacancy vac2 = new Vacancy("X", "Y");
        Vacancy savedVac2 = vacancyRepository.save(vac2);

        responseRepository.save(new UserVacancyResponse(userA, vacancyA));
        responseRepository.save(new UserVacancyResponse(userA, savedVac2.getId()));
        responseRepository.save(new UserVacancyResponse(userB, vacancyA));

        CurrentUser organizationContext = new CurrentUser(orgA, Set.of(Role.ORGANIZATION));

        // Act
        List<UserVacancyResponse> vacancyResponses = getVacancyResponsesUseCase.execute(vacancyA, organizationContext);

        // Assert
        assertNotNull(vacancyResponses);
        assertEquals(2, vacancyResponses.size());
    }

    @Test
    void testGetVacancyResponses_AccessDenied() {
        // Arrange
        CurrentUser differentOrg = new CurrentUser(999L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> getVacancyResponsesUseCase.execute(testVacancy.getId(), differentOrg));
        assertEquals("Вакансия не принадлежит данной организации", ex.getMessage());
    }

    @Test
    void testChangeResponseStatus_Success() {
        // Arrange
        long userId = 33L;
        long vacancyId = testVacancy.getId();
        var saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        CurrentUser organizationContext = new CurrentUser(1L, Set.of(Role.ORGANIZATION));

        // Act
        UserVacancyResponse updated = changeResponseStatusUseCase.execute(
                saved.getId(), UserVacancyResponse.Status.ACCEPTED, organizationContext);

        // Assert
        assertNotNull(updated);
        assertEquals(UserVacancyResponse.Status.ACCEPTED, updated.getStatus());
        verify(vacancyEventPort, times(1)).publishVacancyResponseUpdated(updated);
    }

    @Test
    void testChangeResponseStatus_AccessDenied() {
        // Arrange
        long userId = 44L;
        long vacancyId = testVacancy.getId();
        var saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        CurrentUser differentOrg = new CurrentUser(999L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> changeResponseStatusUseCase.execute(
                        saved.getId(), UserVacancyResponse.Status.IN_PROCESS, differentOrg));
        assertEquals("Вакансия не принадлежит данной организации", ex.getMessage());
    }
}
