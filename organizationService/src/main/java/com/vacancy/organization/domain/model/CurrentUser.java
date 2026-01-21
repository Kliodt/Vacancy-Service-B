package com.vacancy.organization.domain.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Domain модель аутентифицированного пользователя
 * Отвязана от Spring Security
 */
@Getter
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private Set<Role> roles;

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
