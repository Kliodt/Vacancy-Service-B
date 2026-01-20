package com.vacancy.vacancy.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vacancy.vacancy.exceptions.RequestException;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.vacancy.vacancy.kafka.KafkaProducerService;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final KafkaProducerService kafkaProducer;

    public Page<Vacancy> getAllVacancies(int page, int size) {
        if (size > 50)
            size = 50;
        Pageable pageable = PageRequest.of(page, size);
        return vacancyRepository.findAll(pageable);
    }

    public Vacancy getVacancyById(long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, "Вакансия не найдена"));
    }

    public List<Vacancy> getVacanciesByOrganization(long orgId) {
        return vacancyRepository.findByOrganizationId(orgId);
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public void deleteVacancy(long vacancyId) {
        Vacancy vac = getVacancyById(vacancyId);

        if (!vac.getOrganizationId().equals(getPrincipal()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Нельзя удалять вакансии другой организации");

        vacancyRepository.delete(vac);
        kafkaProducer.sendVacancyDeleted(vacancyId);
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public Vacancy updateVacancy(long vacancyId, Vacancy vacancy) {
        Vacancy oldVac = getVacancyById(vacancyId);

        if (!oldVac.getOrganizationId().equals(getPrincipal()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Нельзя изменять вакансии другой организации");

        oldVac.updateWithOther(vacancy);

        return vacancyRepository.save(oldVac);
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZATION')")
    public Vacancy createVacancy(Vacancy vacancy) {
        vacancy.setOrganizationId(getPrincipal());
        return vacancyRepository.save(vacancy);
    }

    private Long getPrincipal() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
