package com.vacancy.organization.service;

import java.util.List;

import com.vacancy.organization.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
    Flux<Organization> getAllOrganizations(int page, int size);
    Mono<Organization> getOrganizationById(Long id);
    Mono<Organization> createOrganization(Organization organization);
    Mono<Organization> updateOrganization(Long id, Organization organization);
    Mono<Void> deleteOrganization(Long id);
    Mono<List<Long>> getOrganizationVacancyIds(Long id);
    Mono<Void> addVacancyToOrganization(Long organizationId, Long vacancyId);
    Mono<Void> updateOrganizationVacancy(Long organizationId, Long vacancyId);
    Mono<Void> deleteOrganizationVacancy(Long organizationId, Long vacancyId);
}
