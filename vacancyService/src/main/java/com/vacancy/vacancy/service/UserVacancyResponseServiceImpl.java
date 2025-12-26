package com.vacancy.vacancy.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.vacancy.client.Clients;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class UserVacancyResponseServiceImpl implements UserVacancyResponseService {

    private final UserVacancyResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;
    private final Clients clients;

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getUserResponses(long userId, Long currUserId) {
        if (!Objects.equals(userId, currUserId))
            throw new RequestException(HttpStatus.FORBIDDEN, "Можно получать только свои отклики");
        return responseRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserVacancyResponse> getVacancyResponses(long vacancyId) {
        return responseRepository.findByVacancyId(vacancyId);
    }

    @Transactional
    public void removeResponseFromVacancy(long vacancyId, long userId, Long currUserId) {
        if (!Objects.equals(userId, currUserId))
            throw new RequestException(HttpStatus.FORBIDDEN, "Можно удалять только свои отклики");
        responseRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }

    public void respondToVacancy(long vacancyId, long userId, Long currUserId) {
        if (!Objects.equals(userId, currUserId))
            throw new RequestException(HttpStatus.FORBIDDEN, "Нельзя оставлять отклики за других");
        
        try {
            clients.getUserById(userId);
        } catch (Exception e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        if (vacancyRepository.findById(vacancyId).isEmpty()) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена");
        }
        List<UserVacancyResponse> existing = responseRepository.findByUserIdAndVacancyId(userId, vacancyId);
        if (!existing.isEmpty()) {
            responseRepository.deleteAll(existing);
        }
        responseRepository.save(new UserVacancyResponse(userId, vacancyId));
    }

}
