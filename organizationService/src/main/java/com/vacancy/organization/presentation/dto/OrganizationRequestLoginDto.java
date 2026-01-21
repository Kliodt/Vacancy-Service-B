package com.vacancy.organization.presentation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - Запрос на вход организации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrganizationRequestLoginDto {

    @NotBlank(message = "Email не может быть пуст")
    @Email(message = "Email должен быть валиден")
    private String email;

    @NotBlank(message = "Пароль не может быть пуст")
    private String password;
}
