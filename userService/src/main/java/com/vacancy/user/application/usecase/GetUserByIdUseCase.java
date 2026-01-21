package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Получить пользователя по ID
 */
@Component
@RequiredArgsConstructor
public class GetUserByIdUseCase {
    private final UserPersistencePort userPersistencePort;

    public Mono<User> execute(long id) {
        return userPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Пользователь не найден")));
    }
}
