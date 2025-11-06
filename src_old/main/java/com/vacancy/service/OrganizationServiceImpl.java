package com.vacancy.service;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vacancy.exceptions.RequestException;
import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_NOT_FOUND = "Организация не найдена";

    private final OrganizationRepository organizationRepository;
    private final VacancyService vacancyService;

    public Page<Organization> getAllOrganizations(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return organizationRepository.findAll(pageable);
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND));
    }

    @Transactional
    public Organization createOrganization(Organization organization) {
        if (organizationRepository.findOrganizationByEmail(organization.getEmail()) != null) {
            throw new RequestException(HttpStatus.CONFLICT, "Организация с таким email уже зарегистрирована");
        }
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization updateOrganization(Long id, Organization organization) {
        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND));

        Organization existingByEmail = organizationRepository.findOrganizationByEmail(organization.getEmail());
        if (existingByEmail != null && existingByEmail.getId() != id) {
            throw new RequestException(HttpStatus.CONFLICT, "С таким email уже зарегистрирована другая организация");
        }

        existingOrganization.setNickname(organization.getNickname());
        existingOrganization.setEmail(organization.getEmail());

        return organizationRepository.save(existingOrganization);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    @Transactional
    public List<Vacancy> getOrganizationVacancies(Long id) {
        List<Vacancy> vacancies = organizationRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND))
                .getPublishedVacancies();
        Hibernate.initialize(vacancies);
        return vacancies;
    }

    @Transactional
    public Vacancy publishVacancy(Long organizationId, Vacancy vacancy) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, ORGANIZATION_NOT_FOUND));
        vacancy.setOrganization(organization);
        return vacancyService.saveVacancy(vacancy);
    }

    @Transactional
    public Vacancy updateOrganizationVacancy(Long organizationId, Long vacancyId, Vacancy vacancy) {
        Vacancy existingVacancy = vacancyService.getVacancyById(vacancyId);

        if (existingVacancy.getOrganization() == null || existingVacancy.getOrganization().getId() != organizationId) {
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");
        }

        existingVacancy.setDescription(vacancy.getDescription());
        existingVacancy.setLongDescription(vacancy.getLongDescription());
        existingVacancy.setMinSalary(vacancy.getMinSalary());
        existingVacancy.setMaxSalary(vacancy.getMaxSalary());
        existingVacancy.setCity(vacancy.getCity());
        
        return vacancyService.saveVacancy(existingVacancy);
    }

    @Transactional
    public void deleteOrganizationVacancy(Long organizationId, Long vacancyId) {
        Vacancy vacancy = vacancyService.getVacancyById(vacancyId);

        if (vacancy.getOrganization() == null || vacancy.getOrganization().getId() != organizationId) {
            throw new RequestException(HttpStatus.FORBIDDEN, "Вакансия не принадлежит данной организации");
        }

        vacancyService.deleteVacancy(vacancy.getId());
    }
}