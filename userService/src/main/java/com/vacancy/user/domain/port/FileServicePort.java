package com.vacancy.user.domain.port;

import reactor.core.publisher.Mono;

/**
 * Domain Port - File Service Client
 */
public interface FileServicePort {
    Mono<Object> getFileById(String fileId);
}
