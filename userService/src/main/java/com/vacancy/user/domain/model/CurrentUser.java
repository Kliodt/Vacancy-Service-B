package com.vacancy.user.domain.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Domain model - Текущий аутентифицированный пользователь
 */
@Getter
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private Set<Role> roles;

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }
}
