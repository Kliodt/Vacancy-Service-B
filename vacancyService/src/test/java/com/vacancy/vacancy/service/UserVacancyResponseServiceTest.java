package com.vacancy.vacancy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

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

import com.vacancy.vacancy.client.UserClient;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Request.HttpMethod;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class) 
@ContextConfiguration 
@WithMockUser(roles = "USER")
class UserVacancyResponseServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;
    @Autowired
    UserVacancyResponseService responseService;
    @MockitoBean
    private UserClient userClient;

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

        // Mock beans
        when(userClient.getUserById(anyLong(), any()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    if (id < 100L)
                        return new Object();
                    Request fakeReq = Request.create(HttpMethod.GET, "/", Collections.emptyMap(), null,
                            StandardCharsets.UTF_8, new RequestTemplate());
                    throw new FeignException.NotFound("Not found", fakeReq, null, null);
                });
    }

    @Test
    void testRespondToVacancy_Success() {
        long userId = 5L;
        // ensure there is a vacancy (testVacancy created in setUp)
        responseService.respondToVacancy(testVacancy.getId(), userId, userId);

        var list = responseRepository.findByUserIdAndVacancyId(userId, testVacancy.getId());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(userId, list.get(0).getUserId().longValue());
        assertEquals(testVacancy.getId(), list.get(0).getVacancyId().longValue());
    }

    @Test
    void testRespondToVacancy_VacancyNotFound() {
        long userId = 7L;
        long missingVacancyId = 99999L;
        // userClient mocked to return object in setUp, but vacancy id doesn't exist
        assertThrows(RequestException.class, () -> {
            responseService.respondToVacancy(missingVacancyId, userId, userId);
        });
    }

    @Test
    void testRespondToVacancy_UserNotFound() {
        long missingUserId = 200L;
        long vacancyId = testVacancy.getId();
        RequestException ex = assertThrows(RequestException.class,
            () -> responseService.respondToVacancy(vacancyId, missingUserId, missingUserId));
        assertEquals(HttpStatus.NOT_FOUND, ex.code);
    }

    @Test
    void testRespondToVacancy_DontDuplicate() {
        long userId = 5L;
        long vacancyId = testVacancy.getId();
        assertEquals(0, responseRepository.count());
        responseService.respondToVacancy(vacancyId, userId, userId);
        responseService.respondToVacancy(vacancyId, userId, userId);
        responseService.respondToVacancy(vacancyId, userId, userId);
        assertEquals(1, responseRepository.count());
    }

    @Test
    void testRemoveResponseFromVacancy() {
        long userId = 11L;
        long vacancyId = testVacancy.getId();
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));

        // ensure saved
        var before = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertEquals(1, before.size());

        responseService.removeResponseFromVacancy(vacancyId, userId, userId);

        var after = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        assertTrue(after == null || after.isEmpty());
    }

    @Test
    void testGetResponsesQueries() {
        long userA = 21L;
        long userB = 22L;
        long vacancyA = testVacancy.getId();
        Vacancy vac2 = new Vacancy("X", "Y");
        vac2.setOrganizationId(1L);
        vac2.setSalary(50000);
        vac2.setCity("City");
        Vacancy savedVac2 = vacancyRepository.save(vac2);

        // create responses
        responseRepository.save(new UserVacancyResponse(userA, vacancyA));
        responseRepository.save(new UserVacancyResponse(userA, savedVac2.getId()));
        responseRepository.save(new UserVacancyResponse(userB, vacancyA));

        var userResponses = responseService.getUserResponses(userA, userA);
        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());

        var vacancyResponses = responseService.getVacancyResponses(vacancyA);
        assertNotNull(vacancyResponses);
        assertEquals(2, vacancyResponses.size());

    }

}
