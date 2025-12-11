package com.vacancy.user.service;

import com.vacancy.user.model.User;
import com.vacancy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.vacancy.user.client.VacancyClient;
import com.vacancy.user.exceptions.RequestException;
import com.vacancy.user.exceptions.ServiceException;

import java.util.List;
import java.util.Set;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String VACANCY_SERVICE_NOT_AVAILABLE = "Сервис вакансий недоступен";

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

    public Mono<User> getUserById(Long id) {
        return Mono.fromCallable(() -> userRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> createUser(User user) {
        return Mono.fromCallable(() -> {
            if (userRepository.findUserByEmail(user.getEmail()) != null) {
                throw new RequestException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
            }
            user.setId(0L);
            return userRepository.save(user);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> updateUser(Long id, User user) {
        return Mono.fromCallable(() -> {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            if (userRepository.findUserByEmail(user.getEmail()) != null
                    && !existingUser.getEmail().equals(user.getEmail())) {
                throw new RequestException(HttpStatus.CONFLICT,
                        "С таким email уже зарегистрирован другой пользователь");
            }

            existingUser.setNickname(user.getNickname());
            existingUser.setEmail(user.getEmail());
            existingUser.setCvLink(user.getCvLink());

            return userRepository.save(existingUser);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteUser(Long id) {
        return Mono.fromRunnable(() -> userRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<Set<Long>> getUserFavoriteVacancyIds(Long id) {
        return getUserById(id)
                .map(User::getFavoriteVacancyIds);
    }

    @CircuitBreaker(name = "vacancyService", fallbackMethod = "getUserFavoritesFallback")
    public Mono<List<Object>> getUserFavorites(Long id) {
        return getUserFavoriteVacancyIds(id)
                .flatMapMany(Flux::fromIterable)
                .flatMap(vacancyId -> Mono.fromCallable(() -> vacancyClient.getVacancyById(vacancyId))
                        .subscribeOn(Schedulers.boundedElastic()))
                .collectList();
    }

    public Mono<List<Object>> getUserFavoritesFallback() {
        return Mono.error(new ServiceException(VACANCY_SERVICE_NOT_AVAILABLE));
    }

    public Mono<List<Object>> getUserResponsesFallback() {
        return Mono.error(new ServiceException(VACANCY_SERVICE_NOT_AVAILABLE));
    }

    @CircuitBreaker(name = "vacancyService", fallbackMethod = "addToFavoritesFallback")
    public Mono<Void> addToFavorites(Long userId, Long vacancyId) {
        return Mono.fromCallable(() -> vacancyClient.getVacancyById(vacancyId))
                .subscribeOn(Schedulers.boundedElastic())
                .then(getUserById(userId))
                .flatMap(user -> Mono.fromCallable(() -> {
                    user.getFavoriteVacancyIds().add(vacancyId);
                    return userRepository.save(user);
                })
                        .subscribeOn(Schedulers.boundedElastic()))
                .then();
    }

    public Mono<Void> addToFavoritesFallback() {
        return Mono.error(new ServiceException(VACANCY_SERVICE_NOT_AVAILABLE));
    }

    public Mono<Void> removeFromFavorites(Long userId, Long vacancyId) {
        return getUserById(userId)
                .flatMap(user -> Mono.fromCallable(() -> {
                    user.getFavoriteVacancyIds().remove(vacancyId);
                    return userRepository.save(user);
                })
                        .subscribeOn(Schedulers.boundedElastic()))
                .then();
    }

    public Mono<Void> respondToVacancyFallback() {
        return Mono.error(new ServiceException(VACANCY_SERVICE_NOT_AVAILABLE));
    }

}
