package com.vacancy.vacancy.service;

import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.model.UserVacancyResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(Long id);
    void deleteVacancy(Long id);
    void respondToVacancy(Long vacancyId, Long userId);
    void removeResponseFromVacancy(Long vacancyId, Long userId);
    Vacancy saveVacancy(Vacancy vacancy);
    List<UserVacancyResponse> getVacancyResponses(Long vacancyId);
}
