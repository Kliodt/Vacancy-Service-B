package com.vacancy.vacancy.service;


import java.util.List;

import com.vacancy.vacancy.model.UserVacancyResponse;

public interface UserVacancyResponseService {
    List<UserVacancyResponse> getUserResponses(long userId);
    List<UserVacancyResponse> getVacancyResponses(long vacancyId);
    void deleteByUserIdAndVacancyId(long userId, long vacancyId);
    UserVacancyResponse addOrReplaceResponse(UserVacancyResponse response);
}
