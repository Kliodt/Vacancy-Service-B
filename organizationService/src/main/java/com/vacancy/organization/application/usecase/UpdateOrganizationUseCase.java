package com.vacancy.organization.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.AccessDeniedException;
import com.vacancy.organization.application.exception.ConflictException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Обновление организации
 * Требует ORGANIZATION роли
 */
@Component
@RequiredArgsConstructor
public class UpdateOrganizationUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;
    private final GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    public Mono<Organization> execute(long organizationId, Organization updated, CurrentUser currentUser) {
        // Проверка прав: только ORGANIZATION может изменять
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            return Mono.error(new AccessDeniedException("Только ORGANIZATION может изменять свои данные"));
        }

        return getOrganizationByIdUseCase.execute(organizationId)
                .flatMap(oldOrg -> {
                    // Проверка владельца
                    if (!oldOrg.getId().equals(currentUser.getId())) {
                        return Mono.error(new AccessDeniedException("Нельзя изменять организацию другого пользователя"));
                    }

                    // Проверка на дублирование email (если email изменился)
                    if (!oldOrg.getEmail().equals(updated.getEmail())) {
                        return organizationPersistencePort.findByEmail(updated.getEmail())
                                .flatMap(existing -> Mono.<Organization>error(
                                        new ConflictException("С таким email уже зарегистрирована другая организация")))
                                .switchIfEmpty(Mono.just(oldOrg))
                                .flatMap(org -> {
                                    org.updateWithOther(updated);
                                    return organizationPersistencePort.save(org);
                                });
                    } else {
                        oldOrg.updateWithOther(updated);
                        return organizationPersistencePort.save(oldOrg);
                    }
                });
    }
}
