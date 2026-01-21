package com.vacancy.user.infrastructure.persistence.adapter;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vacancy.user.application.exception.EntityNotFoundException;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.domain.port.UserPersistencePort;
import com.vacancy.user.infrastructure.persistence.repository.JpaUserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Flux<User> findAll(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            return jpaUserRepository.findAll(pageable).getContent();
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<User> findById(Long id) {
        return Mono.fromCallable(() -> jpaUserRepository.findById(id).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> jpaUserRepository.save(user))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(User user) {
        return Mono.fromRunnable(() -> jpaUserRepository.delete(user))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return Mono.fromCallable(() -> jpaUserRepository.findByEmail(email).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<List<Long>> getFavoriteVacancyIds(Long userId) {
        return Mono
                .fromCallable(() -> jpaUserRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(User::getFavoriteVacancyIds);
    }

    @Override
    public Mono<Void> addToFavorites(Long userId, Long vacancyId) {
        return Mono.fromRunnable(() -> {
            User user = jpaUserRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
            if (!user.getFavoriteVacancyIds().contains(vacancyId)) {
                user.getFavoriteVacancyIds().add(vacancyId);
            }
            jpaUserRepository.save(user);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> removeFromFavorites(Long userId, Long vacancyId) {
        return Mono.fromRunnable(() -> {
            User user = jpaUserRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
            user.getFavoriteVacancyIds().remove(vacancyId);
            jpaUserRepository.save(user);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
