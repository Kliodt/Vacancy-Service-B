package com.vacancy.organization.service;

import com.vacancy.organization.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
    Flux<Organization> getAllOrganizations(int page, int size);
    Mono<Organization> getOrganizationById(long id);
    Mono<Organization> createOrganization(Organization organization, Long currentUserId);
    Mono<Organization> updateOrganization(long id, Organization organization, Long currentUserId);
    Mono<Void> deleteOrganization(long id, Long currentUserId);
    Mono<Organization> updateDirector(long id, Long newDirectorId);
}
