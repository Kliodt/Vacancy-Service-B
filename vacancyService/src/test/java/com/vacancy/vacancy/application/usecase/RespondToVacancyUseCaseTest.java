package com.vacancy.vacancy.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;
import com.vacancy.vacancy.domain.port.VacancyEventPort;

/**
 * Unit тесты для RespondToVacancyUseCase
 * Проверяют создание отклика на вакансию и validation ролей
 */
@ExtendWith(MockitoExtension.class)
class RespondToVacancyUseCaseTest {

    @Mock
    private UserVacancyResponsePersistencePort responseRepository;

    @Mock
    private VacancyEventPort vacancyEventPort;

    @Mock
    private GetVacancyByIdUseCase getVacancyByIdUseCase;

    @InjectMocks
    private RespondToVacancyUseCase respondToVacancyUseCase;

    @Test
    void testExecute_Success() {
        // Arrange
        CurrentUser currentUser = new CurrentUser(5L, Set.of(Role.USER));
        Vacancy vacancy = new Vacancy("Java Dev", "desc");
        vacancy.setId(1L);

        when(getVacancyByIdUseCase.execute(1L)).thenReturn(vacancy);
        when(responseRepository.findByUserIdAndVacancyId(5L, 1L)).thenReturn(Collections.emptyList());
        when(responseRepository.save(any(UserVacancyResponse.class)))
                .thenAnswer(inv -> {
                    UserVacancyResponse r = inv.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        // Act
        UserVacancyResponse result = respondToVacancyUseCase.execute(1L, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getUserId());
        assertEquals(1L, result.getVacancyId());
        verify(vacancyEventPort).publishVacancyResponseCreated(any());
    }

    @Test
    void testExecute_ForbiddenRole_OrganizationRole() {
        // Arrange
        CurrentUser organizationUser = new CurrentUser(1L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> respondToVacancyUseCase.execute(1L, organizationUser));
        assertEquals("Только пользователи могут откликаться на вакансии", ex.getMessage());
    }

    @Test
    void testExecute_ForbiddenRole() {
        // Arrange
        CurrentUser adminUser = new CurrentUser(3L, Set.of(Role.ORGANIZATION));

        // Act & Assert
        ForbiddenRoleException ex = assertThrows(ForbiddenRoleException.class,
                () -> respondToVacancyUseCase.execute(1L, adminUser));
        assertEquals("Только пользователи могут откликаться на вакансии", ex.getMessage());
    }

    @Test
    void testExecute_DuplicateResponseReplaced() {
        // Arrange
        CurrentUser currentUser = new CurrentUser(5L, Set.of(Role.USER));
        Vacancy vacancy = new Vacancy("Java Dev", "desc");
        vacancy.setId(1L);

        UserVacancyResponse existingResponse = new UserVacancyResponse(5L, 1L);
        existingResponse.setId(50L);

        when(getVacancyByIdUseCase.execute(1L)).thenReturn(vacancy);
        when(responseRepository.findByUserIdAndVacancyId(5L, 1L))
                .thenReturn(Collections.singletonList(existingResponse));
        when(responseRepository.save(any(UserVacancyResponse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserVacancyResponse result = respondToVacancyUseCase.execute(1L, currentUser);

        // Assert
        assertNotNull(result);
        verify(responseRepository).deleteAll(anyList());
        verify(responseRepository).save(any(UserVacancyResponse.class));
    }
}
