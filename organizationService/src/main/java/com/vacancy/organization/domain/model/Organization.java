package com.vacancy.organization.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain модель Organization
 * Содержит только бизнес-логику, без Spring/Security зависимостей
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    private Long id;

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private String nickname;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private String email;

    @NotBlank(message = "Password не может быть пустым")
    private String password;

    public void updateWithOther(Organization other) {
        this.setNickname(other.getNickname());
        this.setEmail(other.getEmail());
    }
}
