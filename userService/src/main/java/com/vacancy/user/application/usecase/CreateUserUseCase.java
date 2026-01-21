package com.vacancy.user.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.AccessDeniedException;
import com.vacancy.user.application.exception.ConflictException;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Создание пользователя
 * Требует SUPERVISOR роли
 */
@Component
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> execute(User user, CurrentUser currentUser) {
        // Проверка прав: только SUPERVISOR может создавать пользователей
        if (!currentUser.hasRole(Role.SUPERVISOR)) {
            return Mono.error(new AccessDeniedException("Только SUPERVISOR может создавать пользователей"));
        }

        // Проверка на дублирование email
        return userPersistencePort.findByEmail(user.getEmail())
                .flatMap(existing -> Mono.<User>error(
                        new ConflictException("С таким email уже зарегистрирован другой пользователь")))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    // Хеширование пароля
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return user;
                }).flatMap(userPersistencePort::save));
    }
}
