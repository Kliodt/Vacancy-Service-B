package com.vacancy.organization.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Entity
@Table(name = "organization")
public class Organization {

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "organization_vacancies", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "vacancy_id")
    private Set<Long> vacancyIds = new HashSet<>(); // ID опубликованных вакансий
}
