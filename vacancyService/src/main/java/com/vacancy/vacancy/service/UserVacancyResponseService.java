package com.vacancy.vacancy.service;


import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    List<UserVacancyResponse> getUserResponses(long userId);
    List<UserVacancyResponse> getVacancyResponses(long vacancyId);
    List<UserVacancyResponse> getVacancyResponsesForUser(long userId, long vacancyId);

    void respondToVacancy(long vacancyId, long userId);
    void removeResponseFromVacancy(long vacancyId, long userId);

}
