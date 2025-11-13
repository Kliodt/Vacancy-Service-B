package com.vacancy.user.client;

import org.springframework.stereotype.Component;

@Component
public class VacancyClientFallback implements VacancyClient {

    @Override
    public Object getVacancyById(Long id) {
        throw new RuntimeException("Сервис вакансий недоступен");
    }

}
