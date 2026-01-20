package com.vacancy.vacancy.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.application.exception.ForbiddenRoleException;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Получение откликов пользователя
 * Требует роль USER
 */
@Component
@RequiredArgsConstructor
public class GetUserResponsesUseCase {
    private final UserVacancyResponsePersistencePort responseRepository;

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> execute(CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.USER)) {
            throw new ForbiddenRoleException("Только пользователи могут просматривать свои отклики");
        }
        
        return responseRepository.findByUserId(currentUser.getId());
    }
}
