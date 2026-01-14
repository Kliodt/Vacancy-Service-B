package com.vacancy.organization.service;

import com.vacancy.organization.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
    Flux<Organization> getAllOrganizations(int page, int size);
    Mono<Organization> getOrganizationById(long id);
    Mono<Organization> createOrganization(Organization organization);
    Mono<Organization> updateOrganization(long id, Organization organization);
    Mono<Void> deleteOrganization(long id);
}
