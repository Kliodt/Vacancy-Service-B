package com.vacancy.organization.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.AccessDeniedException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Role;
import com.vacancy.organization.domain.port.OrganizationEventPort;
import com.vacancy.organization.domain.port.OrganizationPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Use Case - Удаление организации
 * Требует ORGANIZATION роли
 */
@Component
@RequiredArgsConstructor
public class DeleteOrganizationUseCase {
    private final OrganizationPersistencePort organizationPersistencePort;
    private final OrganizationEventPort organizationEventPort;
    private final GetOrganizationByIdUseCase getOrganizationByIdUseCase;

    public Mono<Void> execute(long organizationId, CurrentUser currentUser) {
        if (!currentUser.hasRole(Role.ORGANIZATION)) {
            return Mono.error(new AccessDeniedException("Только ORGANIZATION может удалять свой аккаунт"));
        }

        return getOrganizationByIdUseCase.execute(organizationId)
                .flatMap(org -> {
                    // Проверка владельца
                    if (!org.getId().equals(currentUser.getId())) {
                        return Mono.error(new AccessDeniedException("Нельзя удалять организацию другого пользователя"));
                    }
                    return organizationPersistencePort.delete(org)
                            .then(organizationEventPort.publishOrganizationDeleted(organizationId));
                });
    }
}
