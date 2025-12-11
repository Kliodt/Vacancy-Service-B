package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVacancyResponseServiceImpl implements UserVacancyResponseService {

    private final UserVacancyResponseRepository responseRepository;

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getUserResponses(Long userId) {
        return responseRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getVacancyResponses(Long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }

    public void deleteByUserIdAndVacancyId(Long userId, Long vacancyId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    @Transactional
    public UserVacancyResponse addOrReplaceResponse(UserVacancyResponse response) {
        Long vacancyId = response.getVacancyId();
        Long userId = response.getUserId();
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        if (existing != null) {
            responseRepository.deleteAll(existing);
        }
        return responseRepository.save(response);
    }

}
