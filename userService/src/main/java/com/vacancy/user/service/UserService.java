package com.vacancy.user.service;

import com.vacancy.user.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface UserService {
    Flux<User> getAllUsers(int page, int size);
    Mono<User> getUserById(long id);
    Mono<User> createUser(User user);
    Mono<User> updateUser(long id, User user);
    Mono<Void> deleteUser(long id);
    Mono<Set<Long>> getUserFavoriteVacancyIds(long id);
    Mono<List<Object>> getUserFavorites(long id);
    Mono<Object> addToFavorites(long userId, long vacancyId);
    Mono<Void> removeFromFavorites(long userId, long vacancyId);
}
