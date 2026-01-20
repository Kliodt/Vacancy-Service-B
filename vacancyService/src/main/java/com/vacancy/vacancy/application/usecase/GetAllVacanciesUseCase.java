package com.vacancy.vacancy.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

import lombok.RequiredArgsConstructor;

/**
 * Use Case - Получение всех вакансий
 */
@Component
@RequiredArgsConstructor
public class GetAllVacanciesUseCase {
    private final VacancyPersistencePort vacancyPersistencePort;

    public Page<Vacancy> execute(int page, int size) {
        if (size > 50)
            size = 50;
        Pageable pageable = PageRequest.of(page, size);
        return vacancyPersistencePort.findAll(pageable);
    }
}
