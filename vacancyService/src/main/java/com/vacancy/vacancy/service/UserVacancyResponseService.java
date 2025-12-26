package com.vacancy.vacancy.service;


import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    List<UserVacancyResponse> getUserResponses(long userId, Long currUserId);
    List<UserVacancyResponse> getVacancyResponses(long vacancyId);

    void respondToVacancy(long vacancyId, long userId, Long currUserId);
    void removeResponseFromVacancy(long vacancyId, long userId, Long currUserId);

}
