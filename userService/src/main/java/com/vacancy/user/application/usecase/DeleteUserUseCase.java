package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.port.UserEventPort;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Удаление пользователя
 * Требует USER роли
 */
@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final UserEventPort userEventPort;

    public Mono<Void> execute(long userId, CurrentUser currentUser) {
        // Проверка прав: только USER может удалять
        if (!currentUser.hasRole(Role.USER)) {
            return Mono.error(new AccessDeniedException("Только USER может удалять свой аккаунт"));
        }

        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new com.vacancy.user.application.exception.EntityNotFoundException("User with id " + userId + " not found")))
                .flatMap(user -> {
                    // Проверка владельца
                    if (!user.getId().equals(currentUser.getId())) {
                        return Mono.error(new AccessDeniedException("Можно удалять только себя"));
                    }
                    return userPersistencePort.delete(user)
                            .then(userEventPort.publishUserDeleted(userId));
                });
    }
}
