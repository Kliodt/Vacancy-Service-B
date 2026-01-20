package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.application.exception.AccessDeniedException;
import com.vacancy.vacancy.application.exception.EntityNotFoundException;
import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;
import com.vacancy.vacancy.domain.port.VacancyEventPort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Изменение статуса отклика
 * Требует роль ORGANIZATION
 */
@Component
@RequiredArgsConstructor
public class ChangeResponseStatusUseCase {
    private final UserVacancyResponsePersistencePort responseRepository;
    private final VacancyEventPort vacancyEventPort;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;

    @Transactional
    public UserVacancyResponse execute(long responseId, UserVacancyResponse.Status status, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            throw new ForbiddenRoleException("Только организации могут изменять статусы откликов");
        }
        
        UserVacancyResponse resp = responseRepository.findById(responseId).orElseThrow(
                () -> new EntityNotFoundException("Отклик на вакансию не найден"));

        Vacancy vac = getVacancyByIdUseCase.execute(resp.getVacancyId());

        if (!vac.getOrganizationId().equals(currentUser.getId()))
            throw new AccessDeniedException("Вакансия не принадлежит данной организации");

        resp.setStatus(status);
        resp = responseRepository.save(resp);

        vacancyEventPort.publishVacancyResponseUpdated(resp);

        return resp;
    }
}
