package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserPersistencePort;
import com.vacancy.user.domain.port.VacancyServicePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Добавить вакансию в избранное
 */
@Component
@RequiredArgsConstructor
public class AddToFavoritesUseCase {
    private final UserPersistencePort userPersistencePort;
    private final VacancyServicePort vacancyServicePort;

    public Mono<Void> execute(long userId, long vacancyId, CurrentUser currentUser) {
        // Проверка прав
        if (!currentUser.hasRole(Role.USER)) {
            return Mono.error(new AccessDeniedException("Только USER может добавлять в избранное"));
        }

        // Проверка владельца
        if (!currentUser.getId().equals(userId)) {
            return Mono.error(new AccessDeniedException("Можно изменять только свое избранное"));
        }

        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User with id " + userId + " not found")))
                .onErrorMap(err -> new EntityNotFoundException("Пользователь не найден"))
                .flatMap(user -> vacancyServicePort.getVacancyById(vacancyId)
                        .onErrorMap(err -> new EntityNotFoundException("Вакансия не найдена"))
                        .flatMap(idk -> userPersistencePort.addToFavorites(userId, vacancyId))
                        .then());
    }
}
