package com.vacancy.user.model.dto;

import java.util.Set;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vacancy.user.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRequestCreateDto {

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private String nickname;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private String email;

    @NotBlank(message = "Password не может быть пустым")
    private String password;

    @NotNull
    @NotEmpty
    private Set<Role> roles;

}
