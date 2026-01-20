package com.vacancy.vacancy.domain.model;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Entity - Отклик на вакансию
 * Содержит бизнес-логику сущности отклика
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_vacancy_response")
public class UserVacancyResponse {

    public enum Status {
        NONE, IN_PROCESS, ACCEPTED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private @NotNull Long userId;

    @Column(name = "vacancy_id", nullable = false)
    private @NotNull Long vacancyId;

    @Column(name = "response_date", nullable = false)
    private @NotNull OffsetDateTime responseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private @NotNull Status status;

    public UserVacancyResponse(Long userId, Long vacancyId) {
        this.userId = userId;
        this.vacancyId = vacancyId;
        this.responseDate = OffsetDateTime.now();
        this.status = Status.NONE;
    }
}
