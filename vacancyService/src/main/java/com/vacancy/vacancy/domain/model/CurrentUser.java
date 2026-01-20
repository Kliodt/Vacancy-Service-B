package com.vacancy.vacancy.domain.model;

import java.util.Set;

import lombok.Value;

/**
 * Domain Model - информация о текущем пользователе
 * Используется в use cases вместо Authentication
 * Отвязывает бизнес-логику от Spring Security
 */
@Value
public class CurrentUser {
    Long id;
    Set<Role> roles;
    
    public CurrentUser(Long id, Set<Role> roles) {
        this.id = id;
        this.roles = roles;
    }
    
    /**
     * Проверка наличия роли
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
    
    /**
     * Проверка: является ли организацией
     */
    public boolean isOrganization() {
        return hasRole(Role.ORGANIZATION);
    }
    
    /**
     * Проверка: является ли обычным пользователем
     */
    public boolean isUser() {
        return hasRole(Role.USER);
    }
}
