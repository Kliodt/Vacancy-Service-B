package com.vacancy.vacancy.application.usecase;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.application.exception.EntityNotFoundException;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Получение вакансии по ID
 * Публичный, доступен без аутентификации
 */
@Component
@RequiredArgsConstructor
public class GetVacancyByIdUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;

    public Vacancy execute(long id) {
        return vacancyPersistencePort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вакансия не найдена"));
    }
}
