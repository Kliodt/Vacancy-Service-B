package com.vacancy.organization.repository;

import com.vacancy.organization.model.Organization;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrganizationRepository extends R2dbcRepository<Organization, Long> {
    Mono<Organization> findOrganizationByEmail(String email);
}
