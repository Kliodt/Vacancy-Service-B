package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Создание вакансии
 * Требует роль ORGANIZATION
 */
@Component
@RequiredArgsConstructor
public class CreateVacancyUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;

    public Vacancy execute(Vacancy vacancy, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            throw new ForbiddenRoleException("Только организации могут создавать вакансии");
        }
        
        vacancy.setOrganizationId(currentUser.getId());
        return vacancyPersistencePort.save(vacancy);
    }
}
