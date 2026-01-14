package com.vacancy.vacancy.service;

import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    public List<UserVacancyResponse> getUserResponses(long userId);
    public List<UserVacancyResponse> getVacancyResponses(long organizationId, long vacancyId);

    public UserVacancyResponse respondToVacancy(long vacancyId, long userId);
    public void removeResponseFromVacancy(long vacancyId, long userId);

    public UserVacancyResponse changeResponseStatus(long responseId, long organizationId, UserVacancyResponse.Status status);
}
