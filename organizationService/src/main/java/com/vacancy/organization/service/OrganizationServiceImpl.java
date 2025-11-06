package com.vacancy.organization.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.vacancy.organization.model.Organization;
import com.vacancy.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_NOT_FOUND = "Организация не найдена";
    private static final String VACANCY_SERVICE_URL = "http://vacancy-service/api/vacancies";

    private final OrganizationRepository organizationRepository;
    private final RestTemplate restTemplate;

    public Page<Organization> getAllOrganizations(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return organizationRepository.findAll(pageable);
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND));
    }

    @Transactional
    public Organization createOrganization(Organization organization) {
        if (organizationRepository.findOrganizationByEmail(organization.getEmail()) != null) {
            throw new RuntimeException("Организация с таким email уже зарегистрирована");
        }
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization updateOrganization(Long id, Organization organization) {
        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND));

        Organization existingByEmail = organizationRepository.findOrganizationByEmail(organization.getEmail());
        if (existingByEmail != null && existingByEmail.getId() != id) {
            throw new RuntimeException("С таким email уже зарегистрирована другая организация");
        }

        existingOrganization.setNickname(organization.getNickname());
        existingOrganization.setEmail(organization.getEmail());

        return organizationRepository.save(existingOrganization);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Set<Long> getOrganizationVacancyIds(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND))
                .getVacancyIds();
    }

    @Transactional
    public void addVacancyToOrganization(Long organizationId, Long vacancyId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND));
        
        // Verify vacancy exists in vacancy service
        restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class);
        
        organization.getVacancyIds().add(vacancyId);
        organizationRepository.save(organization);
    }

    @Transactional
    public void updateOrganizationVacancy(Long organizationId, Long vacancyId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND));

        if (!organization.getVacancyIds().contains(vacancyId)) {
            throw new RuntimeException("Вакансия не принадлежит данной организации");
        }

        // Verify vacancy exists in vacancy service
        restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class);
    }

    @Transactional
    public void deleteOrganizationVacancy(Long organizationId, Long vacancyId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException(ORGANIZATION_NOT_FOUND));

        if (!organization.getVacancyIds().contains(vacancyId)) {
            throw new RuntimeException("Вакансия не принадлежит данной организации");
        }

        organization.getVacancyIds().remove(vacancyId);
        organizationRepository.save(organization);
    }
}
