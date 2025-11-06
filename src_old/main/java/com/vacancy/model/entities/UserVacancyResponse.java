package com.vacancy.model.entities;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private @NotNull User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private @NotNull Vacancy vacancy;

    @Column(name = "response_date", nullable = false)
    private @NotNull OffsetDateTime responseDate;

    public UserVacancyResponse(@NotNull User user, @NotNull Vacancy vacancy) {
        this.user = user;
        this.vacancy = vacancy;
        this.responseDate = OffsetDateTime.now();
    }
}