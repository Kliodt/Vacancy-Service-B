package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vacancy.vacancy.client.UserClient;
import com.vacancy.vacancy.client.OrganizationClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserVacancyResponseRepository responseRepository;
    private final UserClient userClient;
    private final OrganizationClient organizationClient;

    public Page<Vacancy> getAllVacancies(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return vacancyRepository.findAll(pageable);
    }

    public Vacancy getVacancyById(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));
    }

    public void deleteVacancy(Long id) {
        vacancyRepository.deleteById(id);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "respondToVacancyFallback")
    @Transactional
    public void respondToVacancy(Long vacancyId, Long userId) {
        userClient.getUserById(userId);
        getVacancyById(vacancyId);
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

    public void respondToVacancyFallback(Long vacancyId, Long userId, Exception e) {
        throw new RuntimeException("Сервис пользователей недоступен: " + e.getMessage());
    }

    @Transactional
    public void removeResponseFromVacancy(Long vacancyId, Long userId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    public Vacancy saveVacancy(Vacancy vacancy) {
        if (vacancy.getOrganizationId() != null) {
            organizationClient.getOrganizationById(vacancy.getOrganizationId());
        }
        return vacancyRepository.save(vacancy);
    }

    public List<UserVacancyResponse> getVacancyResponses(Long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }
}
