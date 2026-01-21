package com.vacancy.organization.infrastructure.persistance.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.organization.domain.model.Organization;

import reactor.core.publisher.Mono;

/**
 * R2DBC Repository для Organization
 */
@Repository
public interface OrganizationR2dbcRepository extends R2dbcRepository<Organization, Long> {
    Mono<Organization> findByEmail(String email);
    Mono<Organization> findByNickname(String nickname);
}
