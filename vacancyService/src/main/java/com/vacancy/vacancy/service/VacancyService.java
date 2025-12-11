package com.vacancy.vacancy.service;

import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.model.UserVacancyResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VacancyService {
    Page<Vacancy> getAllVacancies(int page, int size);
    Vacancy getVacancyById(long id);
    void deleteVacancy(long id);
    void respondToVacancy(long vacancyId, long userId);
    void removeResponseFromVacancy(long vacancyId, long userId);
    Vacancy saveVacancy(Vacancy vacancy);
    List<UserVacancyResponse> getVacancyResponses(long vacancyId);
    List<UserVacancyResponse> getUserResponses(long userId);
    List<UserVacancyResponse> getVacancyResponsesForUser(long userId, long vacancyId);
}
