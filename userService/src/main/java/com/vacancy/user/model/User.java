package com.vacancy.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private @NotNull String nickname;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private @NotNull String email;

    @Column(nullable = false, length = 512)
    @Size(max = 512, message = "CV Link не может превышать 512 символов")
    private @Nullable String cvLink;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "vacancy_id")
    private Set<Long> favoriteVacancyIds = new HashSet<>(); // ID избранных вакансий

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_vacancy_responses", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "vacancy_id")
    private Set<Long> responseVacancyIds = new HashSet<>(); // ID вакансий, на которые откликнулся
}
