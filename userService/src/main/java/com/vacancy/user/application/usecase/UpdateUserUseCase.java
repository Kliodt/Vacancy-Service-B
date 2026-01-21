package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.ConflictException;
import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.FileServicePort;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Обновление пользователя
 * Требует USER роли
 */
@Component
@RequiredArgsConstructor
public class UpdateUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final FileServicePort fileServicePort;

    public Mono<User> execute(long userId, User updated, CurrentUser currentUser) {
        // Проверка прав: только USER может изменять
        if (!currentUser.hasRole(Role.USER)) {
            return Mono.error(new AccessDeniedException("Только USER может изменять свои данные"));
        }

        return userPersistencePort.findById(userId)
                .switchIfEmpty(Mono.error(new com.vacancy.user.application.exception.EntityNotFoundException("User with id " + userId + " not found")))
                .flatMap(existingUser -> {
                    // Проверка владельца
                    if (!existingUser.getId().equals(currentUser.getId())) {
                        return Mono.error(new AccessDeniedException("Можно обновлять только свои данные"));
                    }

                    // Проверка на дублирование email (если email изменился)
                    if (!existingUser.getEmail().equals(updated.getEmail())) {
                        return userPersistencePort.findByEmail(updated.getEmail())
                                .flatMap(existing -> Mono.<User>error(
                                        new ConflictException("С таким email уже зарегистрирован другой пользователь")))
                                .switchIfEmpty(Mono.just(existingUser))
                                .flatMap(org -> validateAndUpdate(org, updated));
                    } else {
                        return validateAndUpdate(existingUser, updated);
                    }
                });
    }

    private Mono<User> validateAndUpdate(User existingUser, User updated) {
        String oldCvFile = (existingUser.getCvLink() == null) ? "" : existingUser.getCvLink().trim();
        String newCvFile = (updated.getCvLink() == null) ? "" : updated.getCvLink().trim();

        // If no new file provided or file didn't change, just save
        if (newCvFile.isEmpty() || newCvFile.equals(oldCvFile)) {
            existingUser.updateWithOther(updated);
            return userPersistencePort.save(existingUser);
        }

        // Otherwise, check file, then save
        return fileServicePort.getFileById(newCvFile)
                .onErrorMap(err -> new EntityNotFoundException("Файл не найден"))
                .then(Mono.fromCallable(() -> {
                    existingUser.updateWithOther(updated);
                    return existingUser;
                }).flatMap(userPersistencePort::save));
    }
}
