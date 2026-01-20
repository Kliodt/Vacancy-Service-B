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
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@WithMockUser(roles = "USER")
class UserVacancyResponseServiceTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;
    @Autowired
    UserVacancyResponseService responseService;
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
    void testRespondToVacancy_Success() {
        long userId = 5L;
        // ensure there is a vacancy (testVacancy created in setUp)
        responseService.respondToVacancy(testVacancy.getId(), new TestingAuthenticationToken(userId, null));

        var list = responseRepository.findByUserIdAndVacancyId(userId, testVacancy.getId());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(userId, list.get(0).getUserId().longValue());
        assertEquals(testVacancy.getId(), list.get(0).getVacancyId().longValue());

        // Verify Kafka producer was called with correct arguments
        ArgumentCaptor<UserVacancyResponse> captor = ArgumentCaptor.forClass(UserVacancyResponse.class);
        verify(kafkaProducerService, times(1)).sendVacancyResponseCreated(captor.capture());
        UserVacancyResponse captured = captor.getValue();
        assertEquals(userId, captured.getUserId().longValue());
        assertEquals(testVacancy.getId(), captured.getVacancyId().longValue());
    }

    @Test
    void testRespondToVacancy_VacancyNotFound() {
        long userId = 7L;
        long missingVacancyId = 99999L;
        // userClient mocked to return object in setUp, but vacancy id doesn't exist
        assertThrows(RequestException.class,
                () -> responseService.respondToVacancy(missingVacancyId, new TestingAuthenticationToken(userId, null)));
    }

    @Test
    void testRespondToVacancy_DontDuplicate() {
        long userId = 5L;
        long vacancyId = testVacancy.getId();
        assertEquals(0, responseRepository.count());
        responseService.respondToVacancy(vacancyId, new TestingAuthenticationToken(userId, null));
        responseService.respondToVacancy(vacancyId, new TestingAuthenticationToken(userId, null));
        responseService.respondToVacancy(vacancyId, new TestingAuthenticationToken(userId, null));
        assertEquals(1, responseRepository.count());

        // Verify Kafka producer was called exactly 3 times (once for each respond
        // attempt)
        verify(kafkaProducerService, times(3))
                .sendVacancyResponseCreated(ArgumentCaptor.forClass(UserVacancyResponse.class).capture());
    }

    @Test
    void testRemoveResponseFromVacancy() {
        long userId = 11L;
        long vacancyId = testVacancy.getId();
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        // ensure saved
        var before = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertEquals(1, before.size());

        responseService.removeResponseFromVacancy(vacancyId, new TestingAuthenticationToken(userId, null));

        var after = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertTrue(after == null || after.isEmpty());
    }

    @Test
    void testGetResponsesForUser() {
        Long userA = 21L;
        Long userB = 22L;
        Long vacancyA = testVacancy.getId();

        Vacancy vac2 = new Vacancy("X", "Y");
        Vacancy savedVac2 = vacancyRepository.save(vac2);

        // create responses
        responseRepository.save(new UserVacancyResponse(userA, vacancyA));
        responseRepository.save(new UserVacancyResponse(userA, savedVac2.getId()));
        responseRepository.save(new UserVacancyResponse(userB, vacancyA));

        var userResponses = responseService.getUserResponses(new TestingAuthenticationToken(userA, null));
        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());

    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testGetResponsesForOrganization() {
        Long userA = 21L;
        Long userB = 22L;
        Long vacancyA = testVacancy.getId();
        Long orgA = testVacancy.getOrganizationId();

        Vacancy vac2 = new Vacancy("X", "Y");
        Vacancy savedVac2 = vacancyRepository.save(vac2);

        // create responses
        responseRepository.save(new UserVacancyResponse(userA, vacancyA));
        responseRepository.save(new UserVacancyResponse(userA, savedVac2.getId()));
        responseRepository.save(new UserVacancyResponse(userB, vacancyA));

        var vacancyResponses = responseService.getVacancyResponses(vacancyA, new TestingAuthenticationToken(orgA, null));
        assertNotNull(vacancyResponses);
        assertEquals(2, vacancyResponses.size());
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testChangeResponseStatus_Success() {
        long userId = 33L;
        long vacancyId = testVacancy.getId();
        var saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        var updated = responseService.changeResponseStatus(saved.getId(), UserVacancyResponse.Status.ACCEPTED,
                new TestingAuthenticationToken(1L, null));
        assertNotNull(updated);
        assertEquals(UserVacancyResponse.Status.ACCEPTED, updated.getStatus());

        // Verify Kafka producer was called with updated response and correct status
        ArgumentCaptor<UserVacancyResponse> captor = ArgumentCaptor.forClass(UserVacancyResponse.class);
        verify(kafkaProducerService, times(1)).sendVacancyResponseUpdated(captor.capture());
        UserVacancyResponse captured = captor.getValue();
        assertEquals(UserVacancyResponse.Status.ACCEPTED, captured.getStatus());
        assertEquals(userId, captured.getUserId().longValue());
        assertEquals(vacancyId, captured.getVacancyId().longValue());
    }

    @Test
    @WithMockUser(roles = "ORGANIZATION")
    void testChangeResponseStatus_Forbidden() {
        long userId = 44L;
        long vacancyId = testVacancy.getId();
        var saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        RequestException ex = assertThrows(RequestException.class,
                () -> responseService.changeResponseStatus(saved.getId(), UserVacancyResponse.Status.IN_PROCESS,
                        new TestingAuthenticationToken(999L, null)));
        assertEquals(HttpStatus.FORBIDDEN, ex.code);
    }

}
