package com.vacancy.user.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestUpdateDto {
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private String nickname;

    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private String email;

    @Size(max = 512, message = "CV Link не может превышать 512 символов")
    private String cvLink;
}
