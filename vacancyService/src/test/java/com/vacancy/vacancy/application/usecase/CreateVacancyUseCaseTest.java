package com.vacancy.vacancy.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

/**
 * Unit тесты для CreateVacancyUseCase
 * Проверяют бизнес-логику без Spring контекста
 */
@ExtendWith(MockitoExtension.class)
class CreateVacancyUseCaseTest {

    @Mock
    private VacancyPersistencePort vacancyPersistencePort;

    @InjectMocks
    private CreateVacancyUseCase createVacancyUseCase;

    private Vacancy testVacancy;

    @BeforeEach
    void setUp() {
        testVacancy = new Vacancy("Java Developer", "Develop Java applications");
        testVacancy.setOrganizationId(1L);
        testVacancy.setSalary(100000);
        testVacancy.setCity("Moscow");
    }

    @Test
    void testExecute_Success_OrganizationRole() {
        // Arrange
        CurrentUser currentUser = new CurrentUser(1L, Set.of(Role.ORGANIZATION));
        when(vacancyPersistencePort.save(any(Vacancy.class))).thenReturn(testVacancy);

        // Act
        Vacancy result = createVacancyUseCase.execute(testVacancy, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getOrganizationId());
        assertEquals("Java Developer", result.getTitle());
        verify(vacancyPersistencePort).save(any(Vacancy.class));
    }

    @Test
    void testExecute_ForbiddenRole_UserRole() {
        // Arrange
        CurrentUser userRole = new CurrentUser(5L, Set.of(Role.USER));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> createVacancyUseCase.execute(testVacancy, userRole));
        assertEquals("Только организации могут создавать вакансии", ex.getMessage());
    }

    @Test
    void testExecute_ForbiddenRole() {
        CurrentUser nonOrganization = new CurrentUser(3L, Set.of(Role.USER));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> createVacancyUseCase.execute(testVacancy, nonOrganization));
        assertEquals("Только организации могут создавать вакансии", ex.getMessage());
    }

    @Test
    void testExecute_SetOrganizationId() {
        // Arrange
        CurrentUser currentUser = new CurrentUser(42L, Set.of(Role.ORGANIZATION));
        Vacancy vacancy = new Vacancy("QA", "Test");
        when(vacancyPersistencePort.save(any(Vacancy.class))).thenAnswer(invocation -> {
            Vacancy v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        // Act
        Vacancy result = createVacancyUseCase.execute(vacancy, currentUser);

        // Assert
        assertEquals(42L, result.getOrganizationId());
    }
}
