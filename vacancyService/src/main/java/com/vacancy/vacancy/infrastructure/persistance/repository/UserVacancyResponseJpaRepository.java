package com.vacancy.vacancy.infrastructure.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.vacancy.domain.model.UserVacancyResponse;

import java.util.List;

/**
 * Spring Data JPA Repository - низкоуровневый доступ к БД
 */
@Repository
public interface UserVacancyResponseJpaRepository extends CrudRepository<UserVacancyResponse, Long> {
    List<UserVacancyResponse> findByUserId(Long userId);
    List<UserVacancyResponse> findByVacancyId(Long vacancyId);
    List<UserVacancyResponse> findByUserIdAndVacancyId(Long userId, Long vacancyId);
    void deleteByUserIdAndVacancyId(Long userId, Long vacancyId);
}
