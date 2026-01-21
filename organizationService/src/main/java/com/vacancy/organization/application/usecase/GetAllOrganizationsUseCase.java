package com.vacancy.organization.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * Use Case - Получение всех организаций
 */
@Component
@RequiredArgsConstructor
public class GetAllOrganizationsUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;

    public Flux<Organization> execute(int page, int size) {
        return organizationPersistencePort.findAll(page, size);
    }
}
