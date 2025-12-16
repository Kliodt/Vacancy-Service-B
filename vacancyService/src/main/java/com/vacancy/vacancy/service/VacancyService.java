package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.vacancy.vacancy.model.Vacancy;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(long id);
    void deleteVacancy(long id);
    Vacancy updateVacancy(long id, Vacancy vacancy);

    Vacancy createVacancy(Vacancy vacancy);
    List<Vacancy> getVacanciesByOrganization(long id);
}
