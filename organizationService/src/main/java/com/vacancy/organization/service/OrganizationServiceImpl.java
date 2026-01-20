package com.vacancy.organization.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vacancy.organization.exceptions.RequestException;
import com.vacancy.organization.kafka.KafkaProducerService;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORG_NOT_FOUND_STR = "Организация не найдена";
    private static final String ORG_SAME_EMAIL_STR = "С таким email уже зарегистрирована другая организация";
    private static final String ORG_ACCESS_FORBIDDEN = "Нет доступа к организации";

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducer;

    private Object getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Flux<Organization> getAllOrganizations(int page, int size) {
        if (size > 50)
            size = 50;
        int skip = page * size;
        return organizationRepository.findAll().skip(skip).take(size);
    }

    public Mono<Organization> getOrganizationById(long id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORG_NOT_FOUND_STR)));
    }

    @PreAuthorize("hasRole('ROLE_SUPERVISOR')")
    public Mono<Organization> createOrganization(Organization organization) {
        return organizationRepository.findOrganizationByEmail(organization.getEmail())
                .flatMap(existing -> Mono
                        .<Organization>error(new RequestException(HttpStatus.CONFLICT, ORG_SAME_EMAIL_STR)))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    organization.setPassword(passwordEncoder.encode(organization.getPassword()));
                    return organization;
                }).flatMap(organizationRepository::save));
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public Mono<Organization> updateOrganization(long id, Organization organization) {
        if (!getPrincipal().equals(id)) 
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, ORG_ACCESS_FORBIDDEN));
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RequestException(HttpStatus.NOT_FOUND, ORG_NOT_FOUND_STR)))
                .flatMap(oldOrg -> organizationRepository
                        .findOrganizationByEmail(organization.getEmail())
                        .switchIfEmpty(Mono.just(oldOrg))
                        .flatMap(existingByEmail -> {
                            if (!existingByEmail.getId().equals(id)) {
                                return Mono.error(new RequestException(HttpStatus.CONFLICT, ORG_SAME_EMAIL_STR));
                            }
                            oldOrg.updateWithOther(organization);
                            return organizationRepository.save(oldOrg);
                        }));
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public Mono<Void> deleteOrganization(long id) {
        if (!getPrincipal().equals(id)) 
            return Mono.error(new RequestException(HttpStatus.FORBIDDEN, ORG_ACCESS_FORBIDDEN));
        return Mono.fromRunnable(() -> organizationRepository.deleteById(id))
                .then(kafkaProducer.sendOrganizationDeleted(id))
                .then();
    }

}
