package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final Clients clients;

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

    public List<Vacancy> getVacanciesByOrganization(long id) {
        return vacancyRepository.findByOrganizationId(id);
    }

    public Vacancy updateVacancy(long id, Vacancy vacancy) {
        Vacancy oldVac = vacancyRepository.findById(id).orElse(null);
        if (oldVac == null)
            throw new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена");

        try {
            clients.getOrganizationById(vacancy.getOrganizationId());
        } catch (Exception e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Организация не найдена");
        }

        oldVac.updateWithOther(vacancy);

        return vacancyRepository.save(oldVac);
    }

    public Vacancy createVacancy(Vacancy vacancy) {
        try {
            clients.getOrganizationById(vacancy.getOrganizationId());
        } catch (Exception e) {
            throw new RequestException(HttpStatus.NOT_FOUND, "Организация не найдена");
        }
        return vacancyRepository.save(vacancy);
    }

}
