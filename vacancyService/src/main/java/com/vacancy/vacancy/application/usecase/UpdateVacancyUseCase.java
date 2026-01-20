package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Обновление вакансии
 * Требует роль ORGANIZATION
 */
@Component
@RequiredArgsConstructor
public class UpdateVacancyUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;

    public Vacancy execute(long vacancyId, Vacancy vacancy, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            throw new ForbiddenRoleException("Только организации могут изменять вакансии");
        }
        
        Vacancy oldVac = getVacancyByIdUseCase.execute(vacancyId);

        if (!oldVac.getOrganizationId().equals(currentUser.getId()))
            throw new AccessDeniedException("Нельзя изменять вакансии другой организации");

        oldVac.updateWithOther(vacancy);
        return vacancyPersistencePort.save(oldVac);
    }
}
