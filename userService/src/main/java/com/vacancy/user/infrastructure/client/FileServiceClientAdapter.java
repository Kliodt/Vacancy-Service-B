package com.vacancy.user.infrastructure.client;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vacancy.user.domain.port.FileServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileServiceClientAdapter implements FileServicePort {
    final FileClient fileClient;

    @Override
    @CircuitBreaker(name = "file-service")
    public Mono<Object> getFileById(String fileUUID) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Bearer " + auth.getCredentials())
                .flatMap(authHeader -> fileClient.getFileById(fileUUID, authHeader));
    }
}
