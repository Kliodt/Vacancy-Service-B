package com.vacancy.organization.domain.port;

import reactor.core.publisher.Mono;

/**
 * Domain Port - События организации
 * Используется для отправки событий во внешние системы (Kafka)
 */
public interface OrganizationEventPort {

    /**
     * Отправить событие удаления организации
     */
    Mono<Void> publishOrganizationDeleted(Long organizationId);

    /**
     * Отправить событие входа организации
     */
    Mono<Void> publishOrganizationLoggedIn(Long organizationId);
}
