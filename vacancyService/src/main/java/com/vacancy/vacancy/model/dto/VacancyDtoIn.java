package com.vacancy.vacancy.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VacancyDtoIn {

    @NotBlank(message = "Описание вакансии не может быть пустым")
    @Size(max = 255, message = "Описание не может превышать 255 символов")
    private String description;

    @NotBlank(message = "Подробное описание вакансии не может быть пустым")
    private String longDescription;

    @Min(value = 0, message = "Минимальная зарплата не может быть отрицательной")
    private Integer minSalary;

    @Min(value = 0, message = "Максимальная зарплата не может быть отрицательной")
    private Integer maxSalary;

    @Size(max = 100, message = "Название города не может превышать 100 символов")
    private String city;

    private long organizationId;

    @AssertTrue(message = "Минимальная зарплата не может быть больше максимальной")
    boolean isSalaryRangeValid() {
        return this.minSalary != null && this.maxSalary != null && this.minSalary <= this.maxSalary;
    }
}
