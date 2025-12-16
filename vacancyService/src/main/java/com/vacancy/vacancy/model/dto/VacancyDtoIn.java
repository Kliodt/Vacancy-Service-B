package com.vacancy.vacancy.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String title;

    @NotBlank(message = "Подробное описание вакансии не может быть пустым")
    private String description;

    @Min(value = 0, message = "Зарплата не может быть отрицательной")
    private Integer salary;

    @Size(max = 100, message = "Название города не может превышать 100 символов")
    private String city;

    @NotNull
    private Long organizationId;
    
}
