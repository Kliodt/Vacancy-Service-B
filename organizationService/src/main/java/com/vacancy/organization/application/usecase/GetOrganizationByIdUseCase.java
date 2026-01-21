package com.vacancy.organization.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Получение организации по ID
 */
@Component
@RequiredArgsConstructor
public class GetOrganizationByIdUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;

    public Mono<Organization> execute(long id) {
        return organizationPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Организация не найдена")));
    }
}
