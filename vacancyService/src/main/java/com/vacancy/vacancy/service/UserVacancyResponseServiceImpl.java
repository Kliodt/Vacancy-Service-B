package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.client.UserClient;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVacancyResponseServiceImpl implements UserVacancyResponseService {

    private final UserVacancyResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;
    private final UserClient userClient;

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getUserResponses(long userId) {
        return responseRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getVacancyResponses(long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getVacancyResponsesForUser(long userId, long vacancyId) {
        return responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
    }

    @Transactional
    public void removeResponseFromVacancy(long vacancyId, long userId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    @CircuitBreaker(name = "user-service")
    @Transactional
    public void respondToVacancy(long vacancyId, long userId) {
        try {
            userClient.getUserById(userId);
        } catch (FeignException e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Пользователь на найден");
        }
        if (vacancyRepository.findById(vacancyId).isEmpty()) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена");
        }
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        if (existing != null && !existing.isEmpty()) {
            responseRepository.deleteAll(existing);
        }
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

}
