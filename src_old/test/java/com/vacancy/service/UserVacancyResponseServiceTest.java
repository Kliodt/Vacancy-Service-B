package com.vacancy.service;

import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.User;
import com.vacancy.model.entities.UserVacancyResponse;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.repository.OrganizationRepository;
import com.vacancy.repository.UserRepository;
import com.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.repository.VacancyRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserVacancyResponseServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    UserVacancyResponseService responseService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;

    private User testUser1;
    private User testUser2;
    private Vacancy testVacancy1;
    private Vacancy testVacancy2;
    private Organization testOrganization;

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
        responseRepository.deleteAll();
        vacancyRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrganization = new Organization("TestOrg", "org@example.com");
        testOrganization = organizationRepository.save(testOrganization);

        testUser1 = new User("user1", "user1@example.com");
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User("user2", "user2@example.com");
        testUser2 = userRepository.save(testUser2);

        testVacancy1 = new Vacancy("Java Developer", "Java Developer position");
        testVacancy1.setOrganization(testOrganization);
        testVacancy1 = vacancyRepository.save(testVacancy1);

        testVacancy2 = new Vacancy("Python Developer", "Python Developer position");
        testVacancy2.setOrganization(testOrganization);
        testVacancy2 = vacancyRepository.save(testVacancy2);
    }

    @Test
    void getUserResponsesTest() {
        UserVacancyResponse response1 = new UserVacancyResponse(testUser1, testVacancy1);
        UserVacancyResponse response2 = new UserVacancyResponse(testUser1, testVacancy2);
        responseRepository.save(response1);
        responseRepository.save(response2);

        List<UserVacancyResponse> result = responseService.getUserResponses(testUser1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getVacancy().getId() == testVacancy1.getId()));
        assertTrue(result.stream().anyMatch(r -> r.getVacancy().getId() == testVacancy2.getId()));
        assertTrue(result.stream().allMatch(r -> r.getUser().getId() == testUser1.getId()));
    }

    @Test
    void getUserResponses_shouldReturnEmpty_whenNoResponses() {
        List<UserVacancyResponse> result = responseService.getUserResponses(testUser1.getId());
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserResponses_shouldReturnOnlyUserResponses() {
        UserVacancyResponse response1 = new UserVacancyResponse(testUser1, testVacancy1);
        UserVacancyResponse response2 = new UserVacancyResponse(testUser2, testVacancy1);
        responseRepository.save(response1);
        responseRepository.save(response2);

        List<UserVacancyResponse> result = responseService.getUserResponses(testUser1.getId());

        assertEquals(1, result.size());
        assertEquals(testUser1.getId(), result.get(0).getUser().getId());
        assertEquals(testVacancy1.getId(), result.get(0).getVacancy().getId());
    }

    @Test
    void getVacancyResponsesTest() {
        UserVacancyResponse response1 = new UserVacancyResponse(testUser1, testVacancy1);
        UserVacancyResponse response2 = new UserVacancyResponse(testUser2, testVacancy1);
        responseRepository.save(response1);
        responseRepository.save(response2);

        List<UserVacancyResponse> result = responseService.getVacancyResponses(testVacancy1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getUser().getId() == testUser1.getId()));
        assertTrue(result.stream().anyMatch(r -> r.getUser().getId() == testUser2.getId()));
        assertTrue(result.stream().allMatch(r -> r.getVacancy().getId() == testVacancy1.getId()));
    }

    @Test
    void getVacancyResponses_shouldReturnEmpty_whenNoResponses() {
        List<UserVacancyResponse> result = responseService.getVacancyResponses(testVacancy1.getId());
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getVacancyResponses_shouldReturnOnlyVacancyResponses() {
        UserVacancyResponse response1 = new UserVacancyResponse(testUser1, testVacancy1);
        UserVacancyResponse response2 = new UserVacancyResponse(testUser1, testVacancy2);
        responseRepository.save(response1);
        responseRepository.save(response2);

        List<UserVacancyResponse> result = responseService.getVacancyResponses(testVacancy1.getId());

        assertEquals(1, result.size());
        assertEquals(testUser1.getId(), result.get(0).getUser().getId());
        assertEquals(testVacancy1.getId(), result.get(0).getVacancy().getId());
    }

    @Test
    void responseDtoShouldContainResponseDate() {
        UserVacancyResponse response = new UserVacancyResponse(testUser1, testVacancy1);
        responseRepository.save(response);

        List<UserVacancyResponse> result = responseService.getUserResponses(testUser1.getId());

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getResponseDate());
    }
}