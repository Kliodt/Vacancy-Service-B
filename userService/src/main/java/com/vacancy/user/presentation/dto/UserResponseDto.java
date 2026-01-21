package com.vacancy.user.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;
import com.vacancy.user.domain.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private String cvLink;
    private List<Long> favoriteVacancyIds;
    private Set<Role> roles;
}
