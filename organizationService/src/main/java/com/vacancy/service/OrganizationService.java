package com.vacancy.service;

import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.Vacancy;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrganizationService {
    Page<Organization> getAllOrganizations(int page, int size);
    Organization getOrganizationById(Long id);
    Organization createOrganization(Organization organization);
    Organization updateOrganization(Long id, Organization organization);
    void deleteOrganization(Long id);
    List<Vacancy> getOrganizationVacancies(Long id);
    Vacancy publishVacancy(Long organizationId, Vacancy vacancy);
    Vacancy updateOrganizationVacancy(Long organizationId, Long vacancyId, Vacancy vacancy);
    void deleteOrganizationVacancy(Long organizationId, Long vacancyId);
}
