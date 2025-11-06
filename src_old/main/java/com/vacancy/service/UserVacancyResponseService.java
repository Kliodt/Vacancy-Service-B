package com.vacancy.service;

import com.vacancy.model.entities.UserVacancyResponse;

import java.util.List;

public interface UserVacancyResponseService {
    List<UserVacancyResponse> getUserResponses(Long userId);
    List<UserVacancyResponse> getVacancyResponses(Long vacancyId);
    void deleteByUserIdAndVacancyId(Long userId, Long vacancyId);
    UserVacancyResponse addOrReplaceResponse(UserVacancyResponse response);
}
