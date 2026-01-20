package com.vacancy.vacancy.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.EntityNotFoundException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;
import com.vacancy.vacancy.domain.port.VacancyEventPort;

/**
 * Unit тесты для ChangeResponseStatusUseCase
 * Проверяют изменение статуса отклика с authorization проверками
 */
@ExtendWith(MockitoExtension.class)
class ChangeResponseStatusUseCaseTest {

    @Mock
    private UserVacancyResponsePersistencePort responseRepository;

    @Mock
    private VacancyEventPort vacancyEventPort;

    @Mock
    private GetVacancyByIdUseCase getVacancyByIdUseCase;

    @InjectMocks
    private ChangeResponseStatusUseCase changeResponseStatusUseCase;

    private Vacancy testVacancy;
    private UserVacancyResponse testResponse;

    @BeforeEach
    void setUp() {
        testVacancy = new Vacancy("Java Dev", "desc");
        testVacancy.setId(1L);
        testVacancy.setOrganizationId(100L);

        testResponse = new UserVacancyResponse(5L, 1L);
        testResponse.setId(50L);
    }

    @Test
    void testExecute_Success() {
        // Arrange
        CurrentUser orgUser = new CurrentUser(100L, Set.of(Role.ORGANIZATION));
        when(responseRepository.findById(50L)).thenReturn(Optional.of(testResponse));
        when(getVacancyByIdUseCase.execute(1L)).thenReturn(testVacancy);
        when(responseRepository.save(any(UserVacancyResponse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserVacancyResponse result = changeResponseStatusUseCase.execute(
                50L, UserVacancyResponse.Status.ACCEPTED, orgUser);

        // Assert
        assertNotNull(result);
        assertEquals(UserVacancyResponse.Status.ACCEPTED, result.getStatus());
        verify(vacancyEventPort).publishVacancyResponseUpdated(any());
    }

    @Test
    void testExecute_ForbiddenRole_UserRole() {
        // Arrange
        CurrentUser userRole = new CurrentUser(5L, Set.of(Role.USER));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> changeResponseStatusUseCase.execute(
                        50L, UserVacancyResponse.Status.ACCEPTED, userRole));
        assertEquals("Только организации могут изменять статусы откликов", ex.getMessage());
    }

    @Test
    void testExecute_ResponseNotFound() {
        // Arrange
        CurrentUser orgUser = new CurrentUser(100L, Set.of(Role.ORGANIZATION));
        when(responseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> changeResponseStatusUseCase.execute(
                        999L, UserVacancyResponse.Status.ACCEPTED, orgUser));
        assertEquals("Отклик на вакансию не найден", ex.getMessage());
    }

    @Test
    void testExecute_AccessDenied_DifferentOrganization() {
        // Arrange
        CurrentUser differentOrg = new CurrentUser(999L, Set.of(Role.ORGANIZATION));
        when(responseRepository.findById(50L)).thenReturn(Optional.of(testResponse));
        when(getVacancyByIdUseCase.execute(1L)).thenReturn(testVacancy);

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> changeResponseStatusUseCase.execute(
                        50L, UserVacancyResponse.Status.ACCEPTED, differentOrg));
        assertEquals("Вакансия не принадлежит данной организации", ex.getMessage());
    }
}
