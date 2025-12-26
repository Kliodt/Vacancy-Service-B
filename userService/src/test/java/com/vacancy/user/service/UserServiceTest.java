package com.vacancy.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.user.client.VacancyClient;
import com.vacancy.user.exceptions.RequestException;
import com.vacancy.user.model.User;
import com.vacancy.user.repository.UserRepository;

import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class) 
@ContextConfiguration 
@WithMockUser(roles = "USER")
class UserServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @MockitoBean
    private VacancyClient vacancyClient;

    private User testUser;

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
        registry.add("supervisor.email", () -> "su@su.su");
        registry.add("supervisor.pass", () -> "su");
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();

        // Create test vacancy
        User user = new User("TestUser", "example@gmail.com", "pass");
        user.setCvLink("http://dirve/mycv.txt");
        // Save a test user for use in tests
        testUser = userRepository.save(user);
        assertNotNull(testUser);
    }

    @Test
    void getAllUsers_simple_and_paged() {
        // create additional users
        for (int i = 0; i < 60; i++) {
            User u = new User("User" + i, "u" + i + "@example.com", "pass");
            userRepository.save(u);
        }

        Long count = userRepository.count();

        StepVerifier.create(userService.getAllUsers(0, 60).collectList())
                .expectNextMatches(list -> list.size() == 50)
                .verifyComplete();

        StepVerifier.create(userService.getAllUsers(1, 60).collectList())
                .expectNextMatches(list -> list.size() == count - 50)
                .verifyComplete();
    }

    @Test
    void getUserById_found_and_notFound() {
        StepVerifier.create(userService.getUserById(testUser.getId()))
                .expectNextMatches(u -> u.getEmail().equals(testUser.getEmail()))
                .verifyComplete();

        StepVerifier.create(userService.getUserById(999999L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void createUser_success_and_conflict() {
        User toCreate = new User("NewUser", "newuser@example.com", "pass");
        StepVerifier.create(userService.createUser(toCreate))
                .expectNextMatches(u -> u.getId() > 0 && u.getEmail().equals("newuser@example.com"))
                .verifyComplete();

        User dup = new User("Dup", testUser.getEmail(), "pass");
        StepVerifier.create(userService.createUser(dup))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.CONFLICT, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void updateUser_notFound_conflict_success() {
        // not found
        User upd = new User("X", "x@example.com", "pass");
        StepVerifier.create(userService.updateUser(999999L, upd, 999999L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();

        // conflict: create another user and try to update it to have testUser's email
        User another = new User("Another", "another@example.com", "pass");
        another = userRepository.save(another);
        User conflictUpdate = new User("AnotherUpdated", testUser.getEmail(), "pass");
        StepVerifier.create(userService.updateUser(another.getId(), conflictUpdate, another.getId()))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.CONFLICT, ((RequestException) e).code);
                })
                .verify();

        // success
        User successUpdate = new User("Updated", "updated@example.com", "pass");
        StepVerifier.create(userService.updateUser(testUser.getId(), successUpdate, testUser.getId()))
                .expectNextMatches(u -> u.getEmail().equals("updated@example.com") && u.getNickname().equals("Updated"))
                .verifyComplete();
    }

    @Test
    void deleteUser_completes() {
        StepVerifier.create(userService.deleteUser(testUser.getId(), testUser.getId())).verifyComplete();
    }

    @Test
    void favorites_add_and_remove_flow() {
        // initially empty
        StepVerifier.create(userService.getUserFavoriteVacancyIds(testUser.getId(), testUser.getId()))
                .expectNextMatches(Collection::isEmpty)
                .verifyComplete();

        // mock vacancy client to return found
        when(vacancyClient.getVacancyById(anyLong(), any())).thenReturn(Mono.just(new Object()));

        // add to favorites
        StepVerifier.create(userService.addToFavorites(testUser.getId(), 42L, testUser.getId())).verifyComplete();

        // verify persisted
        User afterAdd = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(afterAdd.getFavoriteVacancyIds().contains(42L));

        // remove
        StepVerifier.create(userService.removeFromFavorites(testUser.getId(), 42L, testUser.getId())).verifyComplete();
        User afterRemove = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(!afterRemove.getFavoriteVacancyIds().contains(42L));
    }

    @Test
    void addToFavorites_vacancyNotFound() {
        // mock vacancy client to return an error (vacancy not found)
        when(vacancyClient.getVacancyById(anyLong(), any())).thenReturn(Mono.error(new RuntimeException("not found")));

        StepVerifier.create(userService.addToFavorites(testUser.getId(), 9999L, testUser.getId()))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();
    }

    @Test
    void addToFavorites_userNotFound() {
        // no user with given id
        StepVerifier.create(userService.addToFavorites(999999L, 1L, 999999L))
                .expectErrorSatisfies(e -> {
                    assertTrue(e instanceof RequestException);
                    assertEquals(HttpStatus.NOT_FOUND, ((RequestException) e).code);
                })
                .verify();
    }

}
