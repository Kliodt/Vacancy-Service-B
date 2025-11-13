package com.vacancy.organization.model;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Table("organization")
public class Organization {

    @Id
    private Long id;

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private String nickname;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private String email;

    private Long[] vacancyIds;


    public List<Long> getVacancyIds() {
        return vacancyIds == null ? List.of() : Arrays.asList(vacancyIds);
    }

    public void setVacancyIds(List<Long> numbers) {
        this.vacancyIds = numbers == null ? null : numbers.toArray(Long[]::new);
    }

}
