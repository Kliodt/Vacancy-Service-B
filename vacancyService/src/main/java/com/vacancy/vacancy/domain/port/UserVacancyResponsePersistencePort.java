package com.vacancy.vacancy.domain.port;

import java.util.List;
import java.util.Optional;

import com.vacancy.vacancy.domain.model.UserVacancyResponse;

// для сохранения UserVacancyResponse
public interface UserVacancyResponsePersistencePort {
    List<UserVacancyResponse> findByUserId(Long userId);
    List<UserVacancyResponse> findByVacancyId(Long vacancyId);
    List<UserVacancyResponse> findByUserIdAndVacancyId(Long userId, Long vacancyId);
    Optional<UserVacancyResponse> findById(Long id);
    UserVacancyResponse save(UserVacancyResponse response);
    void delete(UserVacancyResponse response);
    void deleteAll(List<UserVacancyResponse> responses);
    void deleteByUserIdAndVacancyId(Long userId, Long vacancyId);
}
