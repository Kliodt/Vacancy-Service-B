package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    public List<UserVacancyResponse> getUserResponses(Authentication auth);
    public List<UserVacancyResponse> getVacancyResponses(long vacancyId, Authentication auth);

    public UserVacancyResponse respondToVacancy(long vacancyId, Authentication auth);
    public void removeResponseFromVacancy(long vacancyId, Authentication auth);

    public UserVacancyResponse changeResponseStatus(long responseId, UserVacancyResponse.Status status, Authentication auth);
}
