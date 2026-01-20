package com.vacancy.user.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vacancy.user.client.Clients;
import com.vacancy.user.exceptions.RequestException;
import com.vacancy.user.kafka.KafkaProducerService;
import com.vacancy.user.model.User;
import com.vacancy.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_STR = "Пользователь не найден";
    private static final String USER_SAME_EMAIL_STR = "С таким email уже зарегистрирован другой пользователь";

    private final UserRepository userRepository;
    private final Clients clients;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducer;

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
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_STR)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PreAuthorize("hasRole('ROLE_SUPERVISOR')")
    public Mono<User> createUser(User user) {
        return Mono.fromCallable(() -> {
            if (userRepository.findUserByEmail(user.getEmail()).isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                return userRepository.save(user);
            }
            throw new RequestException(HttpStatus.CONFLICT, USER_SAME_EMAIL_STR);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<User> updateUser(long id, User user, Authentication auth) {
        if (!auth.getPrincipal().equals(id))
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, "Можно обновлять только свои данные"));

        return Mono.fromCallable(() -> {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_STR));

            if (userRepository.findUserByEmail(user.getEmail()).isPresent()
                    && !existingUser.getEmail().equals(user.getEmail())) {
                throw new RequestException(HttpStatus.CONFLICT, USER_SAME_EMAIL_STR);
            }
            return existingUser;
        }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(existingUser -> {
                    String oldCvFile = (existingUser.getCvLink() == null) ? "" : existingUser.getCvLink().trim();
                    String newCvFile = (user.getCvLink() == null) ? "" : user.getCvLink().trim();

                    // If no new file provided or file didn't change, just save
                    if (newCvFile.isEmpty() || newCvFile.equals(oldCvFile)) {
                        existingUser.updateWithOther(user);
                        return Mono.fromCallable(() -> userRepository.save(existingUser))
                                .subscribeOn(Schedulers.boundedElastic());
                    }

                    // Otherwise, check file, then save
                    return clients.getFileById(newCvFile)
                            .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, "CV файл не найден")))
                            .onErrorMap(err -> new RequestException(HttpStatus.NOT_FOUND, "CV файл не найден"))
                            .then(Mono.fromCallable(() -> {
                                existingUser.updateWithOther(user);
                                return userRepository.save(existingUser);
                            }).subscribeOn(Schedulers.boundedElastic()));
                });
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Void> deleteUser(long id, Authentication auth) {
        if (!auth.getPrincipal().equals(id))
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, "Можно удалять только себя"));

        return Mono.fromRunnable(() -> userRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then(kafkaProducer.sendUserDeleted(id))
                .then();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<List<Long>> getUserFavoriteVacancyIds(long id, Authentication auth) {
        if (!auth.getPrincipal().equals(id))
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, "Можно получать только свое избранное"));

        return getUserById(id).map(User::getFavoriteVacancyIds);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Void> addToFavorites(long userId, long vacancyId, Authentication auth) {
        if (!auth.getPrincipal().equals(userId))
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, "Можно изменять только свое избранное"));

        return getUserById(userId)
                .onErrorMap(err -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_STR))
                .flatMap(user -> clients.getVacancyById(vacancyId)
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

    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Void> removeFromFavorites(long userId, long vacancyId, Authentication auth) {
        if (!auth.getPrincipal().equals(userId))
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, "Можно изменять только свое избранное"));

        return getUserById(userId)
                .flatMap(user -> Mono.fromCallable(() -> {
                    user.getFavoriteVacancyIds().remove(vacancyId);
                    return userRepository.save(user);
                }).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
}
