package com.vacancy.organization.service;

import com.vacancy.organization.model.Organization;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface OrganizationService {
    Page<Organization> getAllOrganizations(int page, int size);
    Organization getOrganizationById(Long id);
    Organization createOrganization(Organization organization);
    Organization updateOrganization(Long id, Organization organization);
    void deleteOrganization(Long id);
    Set<Long> getOrganizationVacancyIds(Long id);
    void addVacancyToOrganization(Long organizationId, Long vacancyId);
    void updateOrganizationVacancy(Long organizationId, Long vacancyId);
    void deleteOrganizationVacancy(Long organizationId, Long vacancyId);
}
