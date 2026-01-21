package com.vacancy.organization.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.port.OrganizationEventPort;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Логин организации
 */
@Component
@RequiredArgsConstructor
public class LoginOrganizationUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;
    private final OrganizationEventPort organizationEventPort;

    public Mono<Organization> execute(String email, String password) {
        return organizationPersistencePort.findByEmail(email)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Организация не найдена")))
                .flatMap(org -> {
                    // Проверка пароля (в реальном приложении используется хеширование)
                    if (!org.getPassword().equals(password)) {
                        return Mono.error(new EntityNotFoundException("Неверный пароль"));
                    }
                    return organizationEventPort.publishOrganizationLoggedIn(org.getId())
                            .then(Mono.just(org));
                });
    }
}
