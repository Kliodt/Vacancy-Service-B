package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserVacancyResponseServiceImpl implements UserVacancyResponseService {

    private final UserVacancyResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;

    private Vacancy getVacancyById(long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and #userId == authentication.principal")
    public List<UserVacancyResponse> getUserResponses(long userId) {
        return responseRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ORGANIZATION') and #organizationId == authentication.principal")
    public List<UserVacancyResponse> getVacancyResponses(long organizationId, long vacancyId) {
        Vacancy vac = getVacancyById(vacancyId);
        if (!vac.getOrganizationId().equals(organizationId))
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");
        return responseRepository.findByVacancyId(vacancyId);
    }

    @PreAuthorize("hasRole('ROLE_USER') and #userId == authentication.principal")
    public UserVacancyResponse respondToVacancy(long vacancyId, long userId) {
        getVacancyById(vacancyId);
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        if (!existing.isEmpty()) {
            responseRepository.deleteAll(existing);
        }
        return responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER') and #userId == authentication.principal")
    public void removeResponseFromVacancy(long vacancyId, long userId) {
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ORGANIZATION') and #organizationId == authentication.principal")
    public UserVacancyResponse changeResponseStatus(long responseId, long organizationId, UserVacancyResponse.Status status) {
        UserVacancyResponse resp = responseRepository.findById(responseId).orElseThrow(
                () -> new RequestException(HttpStatus.NOT_FOUND, "Отклик на вакансию не найден"));

        Vacancy vac = getVacancyById(resp.getVacancyId());

        if (!vac.getOrganizationId().equals(organizationId))
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");

        resp.setStatus(status);
        return resp;
    }
}
