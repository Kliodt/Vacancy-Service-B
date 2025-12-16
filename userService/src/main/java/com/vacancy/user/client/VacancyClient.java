package com.vacancy.user.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;


@ReactiveFeignClient(name = "vacancy-service", path = "/api/vacancies")
public interface VacancyClient {

    @GetMapping("/{id}")
    Mono<Object> getVacancyById(@PathVariable("id") Long id);

}
