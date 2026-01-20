package com.vacancy.vacancy.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;
import com.vacancy.vacancy.infrastructure.persistance.repository.VacancyJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter - реализация порта VacancyPersistencePort через Spring Data JPA
 * Это мост между доменом и технологией персистентности
 */
@Component
@RequiredArgsConstructor
public class VacancyPersistenceAdapter implements VacancyPersistencePort {

    private final VacancyJpaRepository jpaRepository;

    @Override
    public Page<Vacancy> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Optional<Vacancy> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Vacancy> findByOrganizationId(Long organizationId) {
        return jpaRepository.findByOrganizationId(organizationId);
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        return jpaRepository.save(vacancy);
    }

    @Override
    public void delete(Vacancy vacancy) {
        jpaRepository.delete(vacancy);
    }

    @Override
    public void deleteAll(List<Vacancy> vacancies) {
        jpaRepository.deleteAll(vacancies);
    }
}
