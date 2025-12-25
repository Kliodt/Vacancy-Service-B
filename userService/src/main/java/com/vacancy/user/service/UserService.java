package com.vacancy.user.service;

import java.util.List;

import com.vacancy.user.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Flux<User> getAllUsers(int page, int size);
    Mono<User> getUserById(long id);
    Mono<User> createUser(User user);
    Mono<User> updateUser(long id, User user, Long currentUserId);
    Mono<Void> deleteUser(long id, Long currentUserId);
    Mono<List<Long>> getUserFavoriteVacancyIds(long id, Long currentUserId);
    Mono<Void> addToFavorites(long userId, long vacancyId, Long currentUserId);
    Mono<Void> removeFromFavorites(long userId, long vacancyId, Long currentUserId);
}
