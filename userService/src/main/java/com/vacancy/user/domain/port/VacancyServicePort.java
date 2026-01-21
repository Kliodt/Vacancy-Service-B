package com.vacancy.user.domain.port;

import reactor.core.publisher.Mono;

/**
 * Domain Port - Vacancy Service Client
 */
public interface VacancyServicePort {
    Mono<Object> getVacancyById(long vacancyId);
}
