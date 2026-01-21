package com.vacancy.user.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Получить избранные вакансии пользователя
 */
@Component
@RequiredArgsConstructor
public class GetUserFavoritesUseCase {
    private final UserPersistencePort userPersistencePort;

    public Mono<List<Long>> execute(long userId, CurrentUser currentUser) {
        // Проверка прав
        if (!currentUser.hasRole(Role.USER)) {
            return Mono.error(new AccessDeniedException("Только USER может получать свое избранное"));
        }

        // Проверка владельца
        if (!currentUser.getId().equals(userId)) {
            return Mono.error(new AccessDeniedException("Можно получать только свое избранное"));
        }

        return userPersistencePort.getFavoriteVacancyIds(userId);
    }
}
