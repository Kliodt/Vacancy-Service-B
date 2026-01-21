package com.vacancy.user.infrastructure.client;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vacancy.user.domain.port.VacancyServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyServiceClientAdapter implements VacancyServicePort {
    final VacancyClient vacancyClient;

    @Override
    @CircuitBreaker(name = "vacancy-service")
    public Mono<Object> getVacancyById(long orgId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Bearer " + auth.getCredentials())
                .flatMap(authHeader -> vacancyClient.getVacancyById(orgId, authHeader));
    }

}
