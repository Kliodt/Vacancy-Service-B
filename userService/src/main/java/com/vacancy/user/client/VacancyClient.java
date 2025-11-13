package com.vacancy.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vacancy-service", path = "/api/vacancies", fallback = VacancyClientFallback.class)
public interface VacancyClient {

    @GetMapping("/{id}")
    Object getVacancyById(@PathVariable("id") Long id);

}
