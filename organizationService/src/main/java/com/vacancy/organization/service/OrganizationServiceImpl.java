package com.vacancy.organization.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vacancy.organization.exceptions.RequestException;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_NOT_FOUND = "Организация не найдена";
    private final OrganizationRepository organizationRepository;

    public Flux<Organization> getAllOrganizations(int page, int size) {
        if (size > 50)
            size = 50;
        int skip = page * size;
        return organizationRepository.findAll().skip(skip).take(size);
    }

    public Mono<Organization> getOrganizationById(long id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)));
    }

    public Mono<Organization> createOrganization(Organization organization, Long currentUserId) {
        organization.setDirector(currentUserId);
        return organizationRepository.findOrganizationByEmail(organization.getEmail())
                .flatMap(existing -> Mono.<Organization>error(new RequestException(HttpStatus.CONFLICT,
                        "С таким email уже зарегистрирована другая организация")))
                .switchIfEmpty(organizationRepository.save(organization));
    }

    public Mono<Organization> updateOrganization(long id, Organization organization, Long currentUserId) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(existingOrganization -> {
                    if (!existingOrganization.getDirector().equals(currentUserId)) {
                        return Mono.error(new RequestException(HttpStatus.FORBIDDEN,
                                "Нет прав на изменение организации"));
                    }
                    return organizationRepository.findOrganizationByEmail(organization.getEmail())
                            .flatMap(existingByEmail -> {
                                if (!existingByEmail.getId().equals(id)) {
                                    return Mono.error(new RequestException(HttpStatus.CONFLICT,
                                            "С таким email уже зарегистрирована другая организация"));
                                }
                                return Mono.empty();
                            })
                            .switchIfEmpty(Mono.just(existingOrganization))
                            .flatMap(org -> {
                                existingOrganization.updateWithOther(organization);
                                existingOrganization.setDirector(existingOrganization.getDirector());
                                return organizationRepository.save(existingOrganization);
                            });
                });
    }

    public Mono<Void> deleteOrganization(long id, Long currentUserId) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(existing -> {
                    if (!existing.getDirector().equals(currentUserId)) {
                        return Mono.error(new RequestException(HttpStatus.FORBIDDEN,
                                "Нет прав на удаление организации"));
                    }
                    return organizationRepository.deleteById(id);
                });
    }

    @PreAuthorize("hasRole('ROLE_SUPERVISOR')")
    public Mono<Organization> updateDirector(long id, Long newDirectorId) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(existing -> {
                    existing.setDirector(newDirectorId);
                    return organizationRepository.save(existing);
                });
    }

}
