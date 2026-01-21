package com.vacancy.organization.domain.port;

import com.vacancy.organization.domain.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Порт для работы с хранилищем организаций
 */
public interface OrganizationPersistencePort {
    Flux<Organization> findAll(int page, int size);
    Mono<Organization> findById(long id);
    Mono<Organization> save(Organization organization);
    Mono<Void> delete(Organization organization);
    Mono<Organization> findByEmail(String email);
    Mono<Organization> findByNickname(String nickname);
}
