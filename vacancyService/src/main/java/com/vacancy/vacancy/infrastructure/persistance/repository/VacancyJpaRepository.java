package com.vacancy.vacancy.infrastructure.persistance.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.vacancy.domain.model.Vacancy;

/**
 * Spring Data JPA Repository - низкоуровневый доступ к БД
 */
@Repository
public interface VacancyJpaRepository extends CrudRepository<Vacancy, Long>, PagingAndSortingRepository<Vacancy, Long> {
    List<Vacancy> findByOrganizationId(Long organizationId);
}
