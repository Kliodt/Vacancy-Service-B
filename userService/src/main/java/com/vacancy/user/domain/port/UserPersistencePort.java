package com.vacancy.user.domain.port;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.vacancy.user.domain.model.User;

/**
 * Domain Port - User Persistence
 * Интерфейс для работы с persistence слоем (БД)
 */
public interface UserPersistencePort {

    Flux<User> findAll(int page, int size);

    Mono<User> findById(Long id);

    Mono<User> save(User user);

    Mono<Void> delete(User user);

    Mono<User> findByEmail(String email);

    Mono<List<Long>> getFavoriteVacancyIds(Long userId);

    Mono<Void> addToFavorites(Long userId, Long vacancyId);

    Mono<Void> removeFromFavorites(Long userId, Long vacancyId);
}
