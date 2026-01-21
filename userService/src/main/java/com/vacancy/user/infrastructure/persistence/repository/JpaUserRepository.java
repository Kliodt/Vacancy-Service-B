package com.vacancy.user.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vacancy.user.domain.model.User;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.favoriteVacancyIds FROM User u WHERE u.id = :userId")
    List<Long> getFavoriteVacancyIds(Long userId);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM User u JOIN u.favoriteVacancyIds v WHERE u.id = :userId AND v = :vacancyId")
    boolean isFavorite(Long userId, Long vacancyId);

    Page<User> findAll(Pageable pageable);
}
