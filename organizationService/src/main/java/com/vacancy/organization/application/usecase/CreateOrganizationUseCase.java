package com.vacancy.organization.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.ConflictException;
import com.vacancy.organization.application.exception.ForbiddenRoleException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Создание организации
 * Требует SUPERVISOR роли
 */
@Component
@RequiredArgsConstructor
public class CreateOrganizationUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public Mono<Organization> execute(Organization organization, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.SUPERVISOR)) {
            return Mono.error(new ForbiddenRoleException("Только SUPERVISOR может создавать организации"));
        }

        // Проверка на дублирование email
        return organizationPersistencePort.findByEmail(organization.getEmail())
                .flatMap(existing -> Mono.<Organization>error(
                        new ConflictException("С таким email уже зарегистрирована другая организация")))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    // Хеширование пароля
                    organization.setPassword(passwordEncoder.encode(organization.getPassword()));
                    return organization;
                }).flatMap(organizationPersistencePort::save));
    }
}
