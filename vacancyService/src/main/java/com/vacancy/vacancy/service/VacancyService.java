package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.vacancy.vacancy.model.Vacancy;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(long id);
    List<Vacancy> getVacanciesByOrganization(long orgId);

    void deleteVacancy(long vacancyId, Authentication auth);
    Vacancy updateVacancy(long vacancyId, Vacancy vacancy, Authentication auth);
    Vacancy createVacancy(Vacancy vacancy, Authentication auth);
}
