package com.vacancy.user.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserEventPort;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Логин пользователя
 */
@Component
@RequiredArgsConstructor
public class LoginUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final UserEventPort userEventPort;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> execute(String email, String password) {
        return userPersistencePort.findByEmail(email)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Пользователь не найден")))
                .flatMap(user -> {
                    // Проверка пароля с использованием PasswordEncoder
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new EntityNotFoundException("Неверный пароль"));
                    }
                    return userEventPort.publishUserLoggedIn(user.getId())
                            .then(Mono.just(user));
                });
    }
}
