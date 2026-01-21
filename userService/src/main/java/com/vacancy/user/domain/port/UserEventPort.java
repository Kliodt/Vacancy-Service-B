package com.vacancy.user.domain.port;

import reactor.core.publisher.Mono;

/**
 * Domain Port - User Events
 * Используется для отправки событий во внешние системы (Kafka)
 */
public interface UserEventPort {

    /**
     * Отправить событие удаления пользователя
     */
    Mono<Void> publishUserDeleted(Long userId);

    /**
     * Отправить событие входа пользователя
     */
    Mono<Void> publishUserLoggedIn(Long userId);
}
