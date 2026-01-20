package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyEventPort;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Удаление вакансии
 * Требует роль ORGANIZATION
 */
@Component
@RequiredArgsConstructor
public class DeleteVacancyUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;
    private final VacancyEventPort vacancyEventPort;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;

    public void execute(long vacancyId, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            throw new ForbiddenRoleException("Только организации могут удалять вакансии");
        }
        
        Vacancy vac = getVacancyByIdUseCase.execute(vacancyId);

        if (!vac.getOrganizationId().equals(currentUser.getId()))
            throw new AccessDeniedException("Нельзя удалять вакансии другой организации");

        vacancyPersistencePort.delete(vac);
        vacancyEventPort.publishVacancyDeleted(vacancyId);
    }
}
