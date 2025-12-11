package com.vacancy.organization.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class VacancyClientFallback implements VacancyClient {

    @Override
    public Object getVacancyById(Long id) {
        Map<String, Object> fallbackVacancy = new HashMap<>();
        fallbackVacancy.put("id", id);
        fallbackVacancy.put("error", "Вакансия временно недоступна");
        return fallbackVacancy;
    }

}
