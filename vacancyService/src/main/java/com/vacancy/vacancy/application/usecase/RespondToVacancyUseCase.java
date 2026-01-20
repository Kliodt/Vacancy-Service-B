package com.vacancy.vacancy.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;
import com.vacancy.vacancy.domain.port.VacancyEventPort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Отклик на вакансию
 * Требует роль USER
 */
@Component
@RequiredArgsConstructor
public class RespondToVacancyUseCase {
    private final UserVacancyResponsePersistencePort responseRepository;
    private final VacancyEventPort vacancyEventPort;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;

    public UserVacancyResponse execute(long vacancyId, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.USER)) {
            throw new ForbiddenRoleException("Только пользователи могут откликаться на вакансии");
        }
        
        getVacancyByIdUseCase.execute(vacancyId);
        Long userId = currentUser.getId();
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);

        if (!existing.isEmpty())
            responseRepository.deleteAll(existing);

        UserVacancyResponse saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));
        vacancyEventPort.publishVacancyResponseCreated(saved);
        return saved;
    }
}
