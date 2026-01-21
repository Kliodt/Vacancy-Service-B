package com.vacancy.organization.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.vacancy.organization.application.exception.EntityNotFoundException;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Role;

/**
 * Преобразователь Spring Security Authentication в domain model CurrentUser
 * Отвязывает use cases от Spring Security
 */
@Component
public class AuthenticationToDomainConverter {
    
    public CurrentUser convert(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new EntityNotFoundException("Не аутентифицирован");
        }
        
        Long organizationId = (Long) auth.getPrincipal();
        Set<Role> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(this::parseRole)
            .collect(Collectors.toSet());
        
        return new CurrentUser(organizationId, roles);
    }

    private Role parseRole(String authority) {
        return switch (authority) {
            case "ROLE_ORGANIZATION" -> Role.ORGANIZATION;
            case "ROLE_SUPERVISOR" -> Role.SUPERVISOR;
            case "ROLE_USER" -> Role.USER;
            default -> Role.ORGANIZATION;
        };
    }
}
