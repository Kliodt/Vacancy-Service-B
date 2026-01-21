package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Удалить вакансию из избранного
 */
@Component
@RequiredArgsConstructor
public class RemoveFromFavoritesUseCase {
    private final UserPersistencePort userPersistencePort;

    public Mono<Void> execute(long userId, long vacancyId, CurrentUser currentUser) {
        // Проверка прав
        if (!currentUser.hasRole(Role.USER)) {
            return Mono.error(new AccessDeniedException("Только USER может удалять из избранного"));
        }

        // Проверка владельца
        if (!currentUser.getId().equals(userId)) {
            return Mono.error(new AccessDeniedException("Можно изменять только свое избранное"));
        }

        return userPersistencePort.removeFromFavorites(userId, vacancyId);
    }
}
