package com.vacancy.organization.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vacancy.organization.client.VacancyClient;
import com.vacancy.organization.exceptions.RequestException;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_NOT_FOUND = "Организация не найдена";
    private final OrganizationRepository organizationRepository;
    private final VacancyClient vacancyClient;

    public Flux<Organization> getAllOrganizations(int page, int size) {
        if (size > 50)
            size = 50;
        int skip = page * size;

        return organizationRepository.findAll()
                .skip(skip)
                .take(size);
    }

    public Mono<Organization> getOrganizationById(long id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)));
    }

    public Mono<Organization> createOrganization(Organization organization) {
        return organizationRepository.findOrganizationByEmail(organization.getEmail())
                .flatMap(existing -> Mono.<Organization>error(new RequestException(HttpStatus.CONFLICT,
                        "С таким email уже зарегистрирована другая организация")))
                .switchIfEmpty(organizationRepository.save(organization));
    }

    public Mono<Organization> updateOrganization(long id, Organization organization) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(existingOrganization -> organizationRepository.findOrganizationByEmail(organization.getEmail())
                        .flatMap(existingByEmail -> {
                            if (!existingByEmail.getId().equals(id)) {
                                return Mono.error(new RequestException(HttpStatus.CONFLICT,
                                        "С таким email уже зарегистрирована другая организация"));
                            }
                            return Mono.just(existingOrganization);
                        })
                        .switchIfEmpty(Mono.just(existingOrganization))
                        .flatMap(org -> {
                            existingOrganization.setNickname(organization.getNickname());
                            existingOrganization.setEmail(organization.getEmail());
                            return organizationRepository.save(existingOrganization);
                        }));
    }

    public Mono<Void> deleteOrganization(long id) {
        return organizationRepository.deleteById(id);
    }

    @CircuitBreaker(name = "vacancy-service")
    public Mono<List<Object>> getOrganizationVacancies(long id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .map(org -> org.getVacancyIds())
                .flatMapMany(Flux::fromIterable) // get organization vacancies ids
                .flatMap(vacId -> Mono.fromCallable(() -> vacancyClient.getVacancyById(vacId))
                        .subscribeOn(Schedulers.boundedElastic()))
                .collectList();
    }

    @CircuitBreaker(name = "vacancy-service")
    public Mono<Void> addVacancyToOrganization(long organizationId, long vacancyId) {
        return organizationRepository.findById(organizationId)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(org -> Mono.fromCallable(() -> vacancyClient.getVacancyById(vacancyId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .then(Mono.fromCallable(() -> {
                            List<Long> ids = org.getVacancyIds();
                            ids.add(vacancyId);
                            org.setVacancyIds(ids);
                            return org;
                        }))
                        .flatMap(organizationRepository::save)
                        .then());
    }

    public Mono<Void> deleteOrganizationVacancy(long organizationId, long vacancyId) {
        return organizationRepository.findById(organizationId)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND)))
                .flatMap(org -> {
                    List<Long> ids = org.getVacancyIds();
                    if (!ids.contains(vacancyId)) {
                        return Mono.error(new RequestException(HttpStatus.FORBIDDEN,
                                "Вакансия не принадлежит данной организации"));
                    }
                    ids.remove(vacancyId);
                    org.setVacancyIds(ids);
                    return organizationRepository.save(org).then();
                });
    }

}
