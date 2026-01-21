package com.vacancy.organization.infrastructure.persistance.adapter;

import org.springframework.stereotype.Component;

import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;
import com.vacancy.organization.infrastructure.persistance.repository.OrganizationR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Адаптер для преобразования R2DBC Repository в Domain Port
 */
@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationPersistencePort {
    private final OrganizationR2dbcRepository repository;

    @Override
    public Flux<Organization> findAll(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        return repository.findAll()
                .skip((long) page * size)
                .take(size);
    }

    @Override
    public Mono<Organization> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        return repository.save(organization);
    }

    @Override
    public Mono<Void> delete(Organization organization) {
        return repository.delete(organization);
    }

    @Override
    public Mono<Organization> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Mono<Organization> findByNickname(String nickname) {
        return repository.findByNickname(nickname);
    }
}
