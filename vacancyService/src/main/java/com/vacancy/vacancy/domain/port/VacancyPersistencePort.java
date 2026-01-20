package com.vacancy.vacancy.domain.port;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vacancy.vacancy.domain.model.Vacancy;

// для сохранения Vacancy
public interface VacancyPersistencePort {
    Page<Vacancy> findAll(Pageable pageable);
    Optional<Vacancy> findById(Long id);
    List<Vacancy> findByOrganizationId(Long organizationId);
    Vacancy save(Vacancy vacancy);
    void delete(Vacancy vacancy);
    void deleteAll(List<Vacancy> vacancies);
}
