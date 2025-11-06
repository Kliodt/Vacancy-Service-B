package com.vacancy.repository;

import com.vacancy.model.entities.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization, Long>, PagingAndSortingRepository<Organization, Long> {
    Organization findOrganizationByEmail(String email);
}
