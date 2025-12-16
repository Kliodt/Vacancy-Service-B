package com.vacancy.vacancy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Entity
@Table(name = "vacancy")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @NotBlank(message = "Описание вакансии не может быть пустым")
    @Size(max = 255, message = "Описание не может превышать 255 символов")
    private @NotNull String title;

    @Column(nullable = false, columnDefinition = "text")
    @NotBlank(message = "Подробное описание вакансии не может быть пустым")
    private @NotNull String description;

    @Min(value = 0, message = "Зарплата не может быть отрицательной")
    private @Nullable Integer salary;

    @Size(max = 100, message = "Название города не может превышать 100 символов")
    private @Nullable String city;

    @Column(nullable = false)
    private Long organizationId;

    public void updateWithOther(Vacancy other) {
        this.setCity(other.getCity());
        this.setTitle(other.getTitle());
        this.setDescription(other.getDescription());
        this.setSalary(other.getSalary());
        this.setOrganizationId(other.getOrganizationId());
    }
}