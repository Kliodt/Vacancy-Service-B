package com.vacancy.user.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vacancy.user.client.VacancyClient;
import com.vacancy.user.exceptions.RequestException;
import com.vacancy.user.model.User;
import com.vacancy.user.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final UserRepository userRepository;
    private final VacancyClient vacancyClient;

    public Flux<User> getAllUsers(int page, int size) {
        if (size > 50)
            size = 50;
        int skip = page * size;

        return Mono.fromCallable(userRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .skip(skip)
                .take(size)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> getUserById(long id) {
        return Mono.fromCallable(() -> userRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> createUser(User user) {
        return Mono.fromCallable(() -> {
            if (userRepository.findUserByEmail(user.getEmail()).isEmpty()) {
                return userRepository.save(user);
            }
            throw new RequestException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> updateUser(long id, User user) {
        return Mono.fromCallable(() -> {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            if (userRepository.findUserByEmail(user.getEmail()).isPresent()
                    && !existingUser.getEmail().equals(user.getEmail())) {
                throw new RequestException(HttpStatus.CONFLICT,
                        "С таким email уже зарегистрирован другой пользователь");
            }
            existingUser.updateWithOther(user);
            return userRepository.save(existingUser);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteUser(long id) {
        return Mono.fromRunnable(() -> userRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<List<Long>> getUserFavoriteVacancyIds(long id) {
        return getUserById(id)
                .map(User::getFavoriteVacancyIds);
    }

    @CircuitBreaker(name = "vacancy-service")
    private Mono<Object> getVacancyById(Long vacancyId) {
        return vacancyClient.getVacancyById(vacancyId);
    }

    public Mono<Void> addToFavorites(long userId, long vacancyId) {
        return getUserById(userId)
                .onErrorMap(err -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND))
                .flatMap(user -> getVacancyById(vacancyId)
                        .onErrorMap(err -> new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена"))
                        .flatMap(idk -> {
                            if (!user.getFavoriteVacancyIds().contains(vacancyId)) {
                                user.getFavoriteVacancyIds().add(vacancyId);
                            }
                            return Mono.fromRunnable(() -> userRepository.save(user))
                                    .subscribeOn(Schedulers.boundedElastic());
                        })
                        .then());
    }

    public Mono<Void> removeFromFavorites(long userId, long vacancyId) {
        return getUserById(userId)
                .flatMap(user -> Mono.fromCallable(() -> {
                    user.getFavoriteVacancyIds().remove(vacancyId);
                    return userRepository.save(user);
                }).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
}
