package com.vacancy.vacancy.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.port.UserVacancyResponsePersistencePort;
import com.vacancy.vacancy.infrastructure.persistance.repository.UserVacancyResponseJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter - реализация порта UserVacancyResponsePersistencePort через Spring Data JPA
 * Это мост между доменом и технологией персистентности
 */
@Component
@RequiredArgsConstructor
public class UserVacancyResponsePersistenceAdapter implements UserVacancyResponsePersistencePort {

    private final UserVacancyResponseJpaRepository jpaRepository;

    @Override
    public List<UserVacancyResponse> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<UserVacancyResponse> findByVacancyId(Long vacancyId) {
        return jpaRepository.findByVacancyId(vacancyId);
    }

    @Override
    public List<UserVacancyResponse> findByUserIdAndVacancyId(Long userId, Long vacancyId) {
        return jpaRepository.findByUserIdAndVacancyId(userId, vacancyId);
    }

    @Override
    public Optional<UserVacancyResponse> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public UserVacancyResponse save(UserVacancyResponse response) {
        return jpaRepository.save(response);
    }

    @Override
    public void delete(UserVacancyResponse response) {
        jpaRepository.delete(response);
    }

    @Override
    public void deleteAll(List<UserVacancyResponse> responses) {
        jpaRepository.deleteAll(responses);
    }

    @Override
    public void deleteByUserIdAndVacancyId(Long userId, Long vacancyId) {
        jpaRepository.deleteByUserIdAndVacancyId(userId, vacancyId);
    }
}
