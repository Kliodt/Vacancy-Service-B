package com.vacancy.vacancy.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vacancy.vacancy.client.Clients;
import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final Clients clients;

    private boolean canThisUserEditOrganizationVacancies(Long userId, Long orgId) {
        Object organization;
        try {
            organization = clients.getOrganizationById(orgId);
        } catch (Exception e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Организация не найдена");
        }
        if (organization instanceof Map<?, ?> map) {
            Object value = map.get("director");
            if (value instanceof Number number) {
                long directorId = number.longValue();
                return Objects.equals(userId, directorId);
            }
        }
        return false;
    }

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

    public void deleteVacancy(long id, Long currUserId) {
        Vacancy vacancy = getVacancyById(id);
        if (!canThisUserEditOrganizationVacancies(currUserId, vacancy.getOrganizationId())) {
            throw new RequestException(HttpStatus.FORBIDDEN, "Нет прав на удаление вакансий этой организации");
        }
        vacancyRepository.deleteById(id);
    }

    public List<Vacancy> getVacanciesByOrganization(long id) {
        return vacancyRepository.findByOrganizationId(id);
    }

    public Vacancy updateVacancy(long id, Vacancy vacancy, Long currUserId) {
        Vacancy oldVac = getVacancyById(id);

        if (!canThisUserEditOrganizationVacancies(currUserId, oldVac.getOrganizationId())) {
            throw new RequestException(HttpStatus.FORBIDDEN, "Нет прав на изменение вакансий этой организации");
        }

        try {
            clients.getOrganizationById(vacancy.getOrganizationId());
        } catch (Exception e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Организация не найдена");
        }

        oldVac.updateWithOther(vacancy);

        return vacancyRepository.save(oldVac);
    }

    public Vacancy createVacancy(Vacancy vacancy, Long currUserId) {
        if (!canThisUserEditOrganizationVacancies(currUserId, vacancy.getOrganizationId())) {
            throw new RequestException(HttpStatus.FORBIDDEN, "Нет прав на создание вакансий этой организации");
        }
        return vacancyRepository.save(vacancy);
    }

}
