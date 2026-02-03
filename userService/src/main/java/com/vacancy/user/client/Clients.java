package com.vacancy.user.client;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class Clients {

    final VacancyClient vacancyClient;
    final FileClient fileClient;

    @CircuitBreaker(name = "vacancy-service")
    public Mono<Object> getVacancyById(long orgId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Bearer " + auth.getCredentials())
                .flatMap(authHeader -> vacancyClient.getVacancyById(orgId, authHeader));
    }

    @CircuitBreaker(name = "file-service")
    public Mono<Object> getFileById(String fileUUID) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Bearer " + auth.getCredentials())
                .flatMap(authHeader -> fileClient.getFileById(fileUUID, authHeader));
    }

}
