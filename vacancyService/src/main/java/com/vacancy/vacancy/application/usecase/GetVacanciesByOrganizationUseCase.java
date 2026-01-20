package com.vacancy.vacancy.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Получение вакансий по ID организации
 */
@Component
@RequiredArgsConstructor
public class GetVacanciesByOrganizationUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;

    public List<Vacancy> execute(long organizationId) {
        return vacancyPersistencePort.findByOrganizationId(organizationId);
    }
}
