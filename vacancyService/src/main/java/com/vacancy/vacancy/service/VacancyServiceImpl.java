package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.client.UserClient;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserVacancyResponseRepository responseRepository;
    private final UserClient userClient;

    public Page<Vacancy> getAllVacancies(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return vacancyRepository.findAll(pageable);
    }

    public Vacancy getVacancyById(long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
    }

    public void deleteVacancy(long id) {
        vacancyRepository.deleteById(id);
    }

    @CircuitBreaker(name = "user-service")
    @Transactional
    public void respondToVacancy(long vacancyId, long userId) {
        userClient.getUserById(userId);
        getVacancyById(vacancyId);
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

    @Transactional
    public void removeResponseFromVacancy(long vacancyId, long userId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    public Vacancy saveVacancy(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public List<UserVacancyResponse> getVacancyResponses(long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }

    public List<UserVacancyResponse> getUserResponses(long userId) {
        return responseRepository.findByUserId(userId);
    }

    public List<UserVacancyResponse> getVacancyResponsesForUser(long userId, long vacancyId) {
        return responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
    }

}
