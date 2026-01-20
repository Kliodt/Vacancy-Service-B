package com.vacancy.user.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.vacancy.user.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Flux<User> getAllUsers(int page, int size);
    Mono<User> getUserById(long id);
    Mono<User> createUser(User user);
    Mono<User> updateUser(long id, User user, Authentication auth);
    Mono<Void> deleteUser(long id, Authentication auth);
    Mono<List<Long>> getUserFavoriteVacancyIds(long id, Authentication auth);
    Mono<Void> addToFavorites(long userId, long vacancyId, Authentication auth);
    Mono<Void> removeFromFavorites(long userId, long vacancyId, Authentication auth);
}
