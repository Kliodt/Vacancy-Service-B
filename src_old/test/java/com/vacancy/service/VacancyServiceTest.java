package com.vacancy.service;


import com.vacancy.exceptions.RequestException;
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
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VacancyServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    VacancyService vacancyService;
    @Autowired
    VacancyRepository vacancyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserVacancyResponseRepository responseRepository;
    @Autowired
    UserService userService;

    private User testUser;
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
        responseRepository.deleteAll();
        vacancyRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testUser = new User("testUser", "test@example.com");
        testUser.setCvLink("http://cv.example.com");
        testUser = userRepository.save(testUser);

        testOrganization = new Organization("TestOrg", "org@example.com");
        testOrganization = organizationRepository.save(testOrganization);

        testVacancy = new Vacancy("Java Developer", "Java Developer position");
        testVacancy.setOrganization(testOrganization);
        testVacancy = vacancyRepository.save(testVacancy);
    }

    @Test
    void getAllVacanciesTest() {
        vacancyRepository.save(new Vacancy("V_1", "V_1"));
        vacancyRepository.save(new Vacancy("V_2", "V_2"));
        vacancyRepository.save(new Vacancy("V_3", "V_3"));

        Page<Vacancy> result = vacancyService.getAllVacancies(0, 2);

        assertEquals(2, result.getContent().size());
        assertEquals(4, result.getTotalElements());
    }

    @Test
    void getAllVacancies_shouldLimitPageSize() {
        Page<Vacancy> result = vacancyService.getAllVacancies(0, 100);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getVacancyByIdTest() {
        Vacancy result = vacancyService.getVacancyById(testVacancy.getId());
        
        assertEquals(testVacancy.getId(), result.getId());
        assertEquals("Java Developer", result.getDescription());
    }

    @Test
    void getVacancyById_shouldThrowException_whenNotFound() {
        assertThrows(RequestException.class, () -> {
            vacancyService.getVacancyById(999L);
        });
    }

    @Test
    void respondToVacancyTest() {
        vacancyService.respondToVacancy(testVacancy.getId(), testUser.getId());

        assertTrue(responseRepository.existsByUserIdAndVacancyId(testUser.getId(), testVacancy.getId()));
    }

    @Test
    void respondToVacancy_shouldNotCreateDuplicate() {
        vacancyService.respondToVacancy(testVacancy.getId(), testUser.getId());
        vacancyService.respondToVacancy(testVacancy.getId(), testUser.getId());

        long count = responseRepository.count();
        assertEquals(1, count);
    }

    @Test
    void removeResponseFromVacancyTest() {
        UserVacancyResponse response = new UserVacancyResponse(testUser, testVacancy);
        responseRepository.save(response);

        vacancyService.removeResponseFromVacancy(testVacancy.getId(), testUser.getId());

        assertFalse(responseRepository.existsByUserIdAndVacancyId(testUser.getId(), testVacancy.getId()));
    }

    @Test
    void addToFavoritesTest() {
        vacancyService.addToFavorites(testVacancy.getId(), testUser.getId());
        List<Vacancy> fav = userService.getUserFavorites(testUser.getId());
        assertEquals(1, fav.size());
        assertEquals(testVacancy.getId(), fav.get(0).getId());
    }

    @Test
    void addToFavorites_shouldNotCreateDuplicate() {
        vacancyService.addToFavorites(testVacancy.getId(), testUser.getId());
        vacancyService.addToFavorites(testVacancy.getId(), testUser.getId());

        List<Vacancy> fav = userService.getUserFavorites(testUser.getId());
        assertEquals(1, fav.size());
    }

    @Test
    void removeFromFavoritesTest() {
        testUser.getFavoriteList().add(testVacancy);
        userRepository.save(testUser);

        vacancyService.removeFromFavorites(testVacancy.getId(), testUser.getId());
        
        List<Vacancy> fav = userService.getUserFavorites(testUser.getId());
        assertEquals(0, fav.size());
    }
}