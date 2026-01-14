package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.vacancy.vacancy.model.Vacancy;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(long id);
    List<Vacancy> getVacanciesByOrganization(long orgId);

    void deleteVacancy(long organizationId, long vacancyId);
    Vacancy updateVacancy(long organizationId, long vacancyId, Vacancy vacancy);
    Vacancy createVacancy(long organizationId, Vacancy vacancy);
}
