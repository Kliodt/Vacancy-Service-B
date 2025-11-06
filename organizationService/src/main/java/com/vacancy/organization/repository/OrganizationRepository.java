package com.vacancy.organization.repository;

import com.vacancy.organization.model.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization, Long>, PagingAndSortingRepository<Organization, Long> {
    Organization findOrganizationByEmail(String email);
}
