package com.vacancy.organization.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import com.vacancy.organization.client.VacancyClient;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "eureka.client.enabled=false" })
@ActiveProfiles("test")
class OrganizationServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    OrganizationService organizationService;
    @MockitoBean
    private VacancyClient vacancyClient;

    private Organization testOrganization;

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
    }

    @BeforeEach
    void setUp() {
        // Clear repositories
        organizationRepository.deleteAll();

        // Create test vacancy
        Organization org = new Organization();
        org.setEmail("example@gmail.com");
        org.setNickname("TestOrg");
        // testOrganization = organizationRepository.saveAll(Mono.just(org));
        // assertNotNull(testVacancy);

    //     Mock beans
    //     when(userClient.getUserById(anyLong()))
    //             .thenAnswer(invocation -> {
    //                 Long id = invocation.getArgument(0);
    //                 if (id < 100L)
    //                     return new Object();
    //                 Request fakeReq = Request.create(HttpMethod.GET, "/", Collections.emptyMap(), null,
    //                         StandardCharsets.UTF_8, new RequestTemplate());
    //                 throw new FeignException.NotFound("Not found", fakeReq, null, null);
    //             });
    }

    @Test
    void simple() {
        assertEquals(0, 0);
    }

 
}
