package com.vacancy.service;

import java.util.List;

import com.vacancy.model.entities.UserVacancyResponse;
import com.vacancy.model.entities.Vacancy;
import org.springframework.data.domain.Page;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(Long id);
    void deleteVacancy(Long id);
    void respondToVacancy(Long vacancyId, Long userId);
    void removeResponseFromVacancy(Long vacancyId, Long userId);
    void addToFavorites(Long vacancyId, Long userId);
    void removeFromFavorites(Long vacancyId, Long userId);
    Vacancy saveVacancy(Vacancy vacancy);
}
