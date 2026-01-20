package com.vacancy.vacancy.service;

import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    public List<UserVacancyResponse> getUserResponses();
    public List<UserVacancyResponse> getVacancyResponses(long vacancyId);

    public UserVacancyResponse respondToVacancy(long vacancyId);
    public void removeResponseFromVacancy(long vacancyId);

    public UserVacancyResponse changeResponseStatus(long responseId, UserVacancyResponse.Status status);
}
