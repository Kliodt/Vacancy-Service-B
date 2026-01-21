package com.vacancy.user.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.ConflictException;
import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.application.usecase.AddToFavoritesUseCase;
import com.vacancy.user.application.usecase.CreateUserUseCase;
import com.vacancy.user.application.usecase.DeleteUserUseCase;
import com.vacancy.user.application.usecase.GetAllUsersUseCase;
import com.vacancy.user.application.usecase.GetUserByIdUseCase;
import com.vacancy.user.application.usecase.GetUserFavoritesUseCase;
import com.vacancy.user.application.usecase.LoginUserUseCase;
import com.vacancy.user.application.usecase.RemoveFromFavoritesUseCase;
import com.vacancy.user.application.usecase.UpdateUserUseCase;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.FileServicePort;
import com.vacancy.user.domain.port.UserEventPort;
import com.vacancy.user.domain.port.VacancyServicePort;
import com.vacancy.user.infrastructure.persistence.repository.JpaUserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Интеграционные тесты для User Use Cases
 * Тестируют взаимодействие use cases с БД через порты
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class UserUseCaseIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    JpaUserRepository userRepository;

    // Use Cases
    @Autowired
    GetAllUsersUseCase getAllUsersUseCase;

    @Autowired
    GetUserByIdUseCase getUserByIdUseCase;

    @Autowired
    CreateUserUseCase createUserUseCase;

    @Autowired
    UpdateUserUseCase updateUserUseCase;

    @Autowired
    DeleteUserUseCase deleteUserUseCase;

    @Autowired
    GetUserFavoritesUseCase getUserFavoritesUseCase;

    @Autowired
    AddToFavoritesUseCase addToFavoritesUseCase;

    @Autowired
    RemoveFromFavoritesUseCase removeFromFavoritesUseCase;

    @Autowired
    LoginUserUseCase loginUserUseCase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    UserEventPort userEventPort;

    @MockitoBean
    FileServicePort fileServicePort;

    @MockitoBean
    VacancyServicePort vacancyServicePort;

    private User testUser;
    private CurrentUser supervisorUser;
    private CurrentUser userOwner;
    private CurrentUser otherUser;

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
        registry.add("jwt.expiration-ms", () -> 3600000);
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();

        // Create test user
        User jpaUser = new User();
        jpaUser.setEmail("test@example.com");
        jpaUser.setNickname("TestUser");
        jpaUser.setPassword(passwordEncoder.encode("password123"));
        jpaUser.setRoles(new HashSet<>(Set.of(Role.USER)));
        testUser = userRepository.save(jpaUser);
        assertNotNull(testUser);

        // Create test users for role-based tests
        supervisorUser = new CurrentUser(1L, Set.of(Role.SUPERVISOR));
        userOwner = new CurrentUser(testUser.getId(), Set.of(Role.USER));
        otherUser = new CurrentUser(999L, Set.of(Role.USER));

        // Mock event and service ports
        when(userEventPort.publishUserDeleted(anyLong())).thenReturn(Mono.empty());
        when(userEventPort.publishUserLoggedIn(anyLong())).thenReturn(Mono.empty());
        when(fileServicePort.getFileById("")).thenReturn(Mono.empty());
        when(vacancyServicePort.getVacancyById(anyLong())).thenReturn(Mono.empty());
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setNickname("User" + i);
            user.setPassword(passwordEncoder.encode("pass"));
            user.setRoles(new HashSet<>(Set.of(Role.USER)));
            userRepository.save(user);
        }

        // Act & Assert
        StepVerifier.create(getAllUsersUseCase.execute(0, 10).collectList())
                .expectNextMatches(list -> list.size() >= 5)
                .verifyComplete();
    }

    @Test
    void testGetUserById_Success() {
        // Act & Assert
        StepVerifier.create(getUserByIdUseCase.execute(testUser.getId()))
                .expectNextMatches(user -> user.getId().equals(testUser.getId()))
                .verifyComplete();
    }

    @Test
    void testGetUserById_NotFound() {
        // Act & Assert
        StepVerifier.create(getUserByIdUseCase.execute(999L))
                .expectErrorMatches(EntityNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setNickname("NewUser");
        newUser.setPassword("password");
        newUser.setRoles(new HashSet<>(Set.of(Role.USER)));

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(newUser, supervisorUser))
                .expectNextMatches(user -> user.getEmail().equals("newuser@example.com"))
                .verifyComplete();
    }

    @Test
    void testCreateUser_ConflictEmail() {
        // Arrange
        User conflictUser = new User();
        conflictUser.setEmail("test@example.com");
        conflictUser.setNickname("ConflictUser");
        conflictUser.setPassword("password");

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(conflictUser, supervisorUser))
                .expectErrorMatches(ConflictException.class::isInstance)
                .verify();
    }

    @Test
    void testCreateUser_NotSupervisor() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setNickname("NewUser");
        newUser.setPassword("password");

        // Act & Assert
        StepVerifier.create(createUserUseCase.execute(newUser, userOwner))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");
        updatedUser.setEmail("updated@example.com");

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(testUser.getId(), updatedUser, userOwner))
                .expectNextMatches(user -> user.getNickname().equals("UpdatedUser"))
                .verifyComplete();
    }

    @Test
    void testUpdateUser_AccessDenied() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNickname("UpdatedUser");

        // Act & Assert
        StepVerifier.create(updateUserUseCase.execute(testUser.getId(), updatedUser, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testDeleteUser_Success() {
        // Act & Assert
        StepVerifier.create(deleteUserUseCase.execute(testUser.getId(), userOwner))
                .verifyComplete();
    }

    @Test
    void testDeleteUser_AccessDenied() {
        // Act & Assert
        StepVerifier.create(deleteUserUseCase.execute(testUser.getId(), otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testFavorites_AddAndRemove() {
        when(vacancyServicePort.getVacancyById(42)).thenReturn(Mono.just(new Object()));

        // Arrange - favorites are initially empty
        StepVerifier.create(getUserFavoritesUseCase.execute(testUser.getId(), userOwner))
                .expectNextMatches(Collection::isEmpty)
                .verifyComplete();

        // Act - add to favorites
        StepVerifier.create(addToFavoritesUseCase.execute(testUser.getId(), 42L, userOwner))
                .verifyComplete();

        // Assert - verify added
        User updated = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updated);
        assert updated.getFavoriteVacancyIds().contains(42L);

        // Act - remove from favorites
        StepVerifier.create(removeFromFavoritesUseCase.execute(testUser.getId(), 42L, userOwner))
                .verifyComplete();

        // Assert - verify removed
        User final_user = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(final_user);
        assert !final_user.getFavoriteVacancyIds().contains(42L);
    }

    @Test
    void testAddToFavorites_AccessDenied() {
        // Act & Assert
        StepVerifier.create(addToFavoritesUseCase.execute(testUser.getId(), 42L, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testRemoveFromFavorites_AccessDenied() {
        // Act & Assert
        StepVerifier.create(removeFromFavoritesUseCase.execute(testUser.getId(), 42L, otherUser))
                .expectErrorMatches(AccessDeniedException.class::isInstance)
                .verify();
    }

    @Test
    void testLogin_Success() {
        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("test@example.com", "password123"))
                .expectNextMatches(user -> user.getId().equals(testUser.getId()))
                .verifyComplete();
    }

    @Test
    void testLogin_InvalidPassword() {
        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("test@example.com", "wrongpassword"))
                .expectErrorMatches(e -> e instanceof EntityNotFoundException)
                .verify();
    }

    @Test
    void testLogin_UserNotFound() {
        // Act & Assert
        StepVerifier.create(loginUserUseCase.execute("notfound@example.com", "password"))
                .expectErrorMatches(e -> e instanceof EntityNotFoundException)
                .verify();
    }
}
