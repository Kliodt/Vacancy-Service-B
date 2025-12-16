package com.vacancy.vacancy.repository;


import com.vacancy.vacancy.model.Vacancy;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VacancyRepository extends CrudRepository<Vacancy, Long>, PagingAndSortingRepository<Vacancy, Long> {
    List<Vacancy> findByOrganizationId(Long organizationId);
}
