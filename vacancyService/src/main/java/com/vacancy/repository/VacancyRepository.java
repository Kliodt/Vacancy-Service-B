package com.vacancy.repository;


import com.vacancy.model.entities.Vacancy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VacancyRepository extends CrudRepository<Vacancy, Long>, PagingAndSortingRepository<Vacancy, Long> {
}
