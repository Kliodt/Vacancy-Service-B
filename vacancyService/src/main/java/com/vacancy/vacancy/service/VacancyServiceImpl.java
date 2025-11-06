package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service/api/users";

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

    @Transactional
    public void respondToVacancy(Long vacancyId, Long userId) {
        // Verify user exists
        restTemplate.getForObject(USER_SERVICE_URL + "/" + userId, Object.class);
        // Verify vacancy exists
        getVacancyById(vacancyId);
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

    @Transactional
    public void removeResponseFromVacancy(Long vacancyId, Long userId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    public Vacancy saveVacancy(Vacancy vacancy) {
        if (vacancy.getOrganizationId() != null) {
            // Verify organization exists
            restTemplate.getForObject("http://organization-service/api/organizations/" + vacancy.getOrganizationId(), Object.class);
        }
        return vacancyRepository.save(vacancy);
    }

    public List<UserVacancyResponse> getVacancyResponses(Long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }
}
