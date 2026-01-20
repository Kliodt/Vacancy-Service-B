package com.vacancy.vacancy.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Получение откликов на вакансию
 * Требует роль ORGANIZATION
 */
@Component
@RequiredArgsConstructor
public class GetVacancyResponsesUseCase {
    private final UserVacancyResponsePersistencePort responseRepository;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> execute(long vacancyId, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            throw new ForbiddenRoleException("Только организации могут просматривать отклики");
        }
        
        Vacancy vac = getVacancyByIdUseCase.execute(vacancyId);

        if (!vac.getOrganizationId().equals(currentUser.getId()))
            throw new AccessDeniedException("Вакансия не принадлежит данной организации");

        return responseRepository.findByVacancyId(vacancyId);
    }
}
