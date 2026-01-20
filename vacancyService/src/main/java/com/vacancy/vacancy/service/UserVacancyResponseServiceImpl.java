package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducer;

    private Vacancy getVacancyById(long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<UserVacancyResponse> getUserResponses() {
        return responseRepository.findByUserId(getPrincipal());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public List<UserVacancyResponse> getVacancyResponses(long vacancyId) {
        Vacancy vac = getVacancyById(vacancyId);

        if (!vac.getOrganizationId().equals(getPrincipal()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");

        return responseRepository.findByVacancyId(vacancyId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public UserVacancyResponse respondToVacancy(long vacancyId) {
        getVacancyById(vacancyId);
        Long userId = getPrincipal();
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);

        if (!existing.isEmpty())
            responseRepository.deleteAll(existing);

        UserVacancyResponse saved = responseRepository.save(new UserVacancyResponse(userId, vacancyId));
        kafkaProducer.sendVacancyResponseCreated(saved);
        return saved;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    public void removeResponseFromVacancy(long vacancyId) {
        responseRepository.deleteByUserIdAndVacancyId(getPrincipal(), vacancyId);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public UserVacancyResponse changeResponseStatus(long responseId, UserVacancyResponse.Status status) {
        UserVacancyResponse resp = responseRepository.findById(responseId).orElseThrow(
                () -> new RequestException(HttpStatus.NOT_FOUND, "Отклик на вакансию не найден"));

        Vacancy vac = getVacancyById(resp.getVacancyId());

        if (!vac.getOrganizationId().equals(getPrincipal()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");

        resp.setStatus(status);
        resp = responseRepository.save(resp);

        kafkaProducer.sendVacancyResponseUpdated(resp);

        return resp;
    }

    private Long getPrincipal() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
