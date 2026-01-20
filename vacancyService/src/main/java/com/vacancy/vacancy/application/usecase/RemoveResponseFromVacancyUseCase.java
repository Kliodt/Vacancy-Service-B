package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Удаление отклика на вакансию
 * Требует роль USER
 */
@Component
@RequiredArgsConstructor
public class RemoveResponseFromVacancyUseCase {
    private final UserVacancyResponsePersistencePort responseRepository;

    @Transactional
    public void execute(long vacancyId, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.USER)) {
            throw new ForbiddenRoleException("Только пользователи могут удалять свои отклики");
        }
        
        responseRepository.deleteByUserIdAndVacancyId(currentUser.getId(), vacancyId);
    }
}
