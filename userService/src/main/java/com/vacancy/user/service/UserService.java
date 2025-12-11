package com.vacancy.user.service;

import com.vacancy.user.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface UserService {
    Flux<User> getAllUsers(int page, int size);
    Mono<User> getUserById(Long id);
    Mono<User> createUser(User user);
    Mono<User> updateUser(Long id, User user);
    Mono<Void> deleteUser(Long id);
    Mono<Set<Long>> getUserFavoriteVacancyIds(Long id);
    Mono<List<Object>> getUserFavorites(Long id);
    Mono<Void> addToFavorites(Long userId, Long vacancyId);
    Mono<Void> removeFromFavorites(Long userId, Long vacancyId);
}
