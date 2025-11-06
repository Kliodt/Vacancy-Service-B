package com.vacancy.vacancy.service;


import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    List<UserVacancyResponse> getUserResponses(Long userId);
    List<UserVacancyResponse> getVacancyResponses(Long vacancyId);
    void deleteByUserIdAndVacancyId(Long userId, Long vacancyId);
    UserVacancyResponse addOrReplaceResponse(UserVacancyResponse response);
}
