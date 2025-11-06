package com.vacancy.vacancy.model;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_vacancy_response")
public class UserVacancyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vacancy_id", nullable = false)
    private Long vacancyId;

    @Column(name = "response_date", nullable = false)
    private @NotNull OffsetDateTime responseDate;

    public UserVacancyResponse(Long userId, Long vacancyId) {
        this.userId = userId;
        this.vacancyId = vacancyId;
        this.responseDate = OffsetDateTime.now();
    }
}
