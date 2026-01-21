package com.vacancy.user.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.user.domain.port.UserPersistencePort;
import com.vacancy.user.domain.model.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * Use Case - Получить всех пользователей
 */
@Component
@RequiredArgsConstructor
public class GetAllUsersUseCase {
    private final UserPersistencePort userPersistencePort;

    public Flux<User> execute(int page, int size) {
        return userPersistencePort.findAll(page, size);
    }
}
