package com.vacancy.vacancy.presentation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Presentation DTO - для исходящих данных вакансии (API Response)
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VacancyResponseDto {

    private Long id;
    private String title;
    private String description;
    private Integer salary;
    private String city;
    private Long organizationId;
}
