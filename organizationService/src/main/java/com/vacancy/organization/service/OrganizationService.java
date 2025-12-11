package com.vacancy.organization.service;

import java.util.List;

import com.vacancy.organization.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
    Flux<Organization> getAllOrganizations(int page, int size);
    Mono<Organization> getOrganizationById(long id);
    Mono<Organization> createOrganization(Organization organization);
    Mono<Organization> updateOrganization(long id, Organization organization);
    Mono<Void> deleteOrganization(long id);
    Mono<List<Object>> getOrganizationVacancies(long id);
    Mono<Void> addVacancyToOrganization(long organizationId, long vacancyId);
    Mono<Void> deleteOrganizationVacancy(long organizationId, long vacancyId);
}
